package com.aish.mvc.service.doc.impl;

import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.Comment;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.CommentStatus;
import com.aish.mvc.entity.enums.ModerationKeywordType;
import com.aish.mvc.exception.CommentBlockedException;
import com.aish.mvc.exception.ForbiddenException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.CommentRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.DownloadRepository;
import com.aish.mvc.repository.doc.FavoriteRepository;
import com.aish.mvc.repository.doc.RatingRepository;
import com.aish.mvc.repository.doc.ViewHistoryRepository;
import com.aish.mvc.service.ai.ToxicKeywordFilter;
import com.aish.mvc.service.doc.DocumentAccessPort;
import com.aish.mvc.service.notification.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Luồng bình luận: nội dung rỗng bị chặn; trúng từ khoá kiểm duyệt thì mặc định bị CHẶN, chỉ khi
 * người dùng chủ động khiếu nại (dispute) mới được lưu ở trạng thái PENDING_REVIEW kèm lý do;
 * và sửa/xoá chỉ dành cho chính tác giả.
 */
class EngagementServiceImplCommentTest {

    private static final String CURRENT_USER_EMAIL = "me@example.com";
    private static final Long CURRENT_USER_ID = 7L;
    private static final Long OTHER_USER_ID = 99L;
    private static final Long DOCUMENT_ID = 3L;
    private static final Long COMMENT_ID = 11L;

    private final DocDocumentRepository docDocumentRepository = mock(DocDocumentRepository.class);
    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final ToxicKeywordFilter toxicKeywordFilter = mock(ToxicKeywordFilter.class);
    private final AuthAccountRepository authAccountRepository = mock(AuthAccountRepository.class);
    private final DocumentAccessPort documentAccessPort = mock(DocumentAccessPort.class);
    private final NotificationService notificationService = mock(NotificationService.class);

    private EngagementServiceImpl service;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(CURRENT_USER_EMAIL, "n/a"));

        AuthAccount authAccount = new AuthAccount();
        authAccount.setUser(user(CURRENT_USER_ID));
        when(authAccountRepository.findByIdentifier(CURRENT_USER_EMAIL)).thenReturn(Optional.of(authAccount));
        when(docDocumentRepository.findById(DOCUMENT_ID))
                .thenReturn(Optional.of(DocDocument.builder().id(DOCUMENT_ID).build()));
        when(documentAccessPort.isAvailableTo(DOCUMENT_ID, CURRENT_USER_ID)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenAnswer(call -> call.getArgument(0));

        service = new EngagementServiceImpl(
                docDocumentRepository,
                commentRepository,
                mock(FavoriteRepository.class),
                mock(RatingRepository.class),
                mock(DownloadRepository.class),
                mock(ViewHistoryRepository.class),
                authAccountRepository,
                toxicKeywordFilter,
                notificationService,
                mock(ApplicationEventPublisher.class),
                documentAccessPort);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static AuthUser user(Long id) {
        AuthUser authUser = new AuthUser();
        authUser.setId(id);
        return authUser;
    }

    private void givenKeywordHit(boolean hit) {
        when(toxicKeywordFilter.matches(anyString(), eq(ModerationKeywordType.COMMENT))).thenReturn(hit);
    }

    private void givenExistingComment(Long authorId) {
        Comment comment = Comment.builder()
                .id(COMMENT_ID)
                .document(DocDocument.builder().id(DOCUMENT_ID).build())
                .user(user(authorId))
                .content("nội dung cũ")
                .status(CommentStatus.VISIBLE)
                .build();
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));
    }

    @Test
    void blankCommentIsRejected() {
        givenKeywordHit(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.addComment(DOCUMENT_ID, "   ", false, null));

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void commentHittingKeywordIsBlockedByDefault() {
        givenKeywordHit(true);

        assertThrows(CommentBlockedException.class,
                () -> service.addComment(DOCUMENT_ID, "nội dung xấu", false, null));

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void disputedCommentIsSavedAsPendingReviewWithReason() {
        givenKeywordHit(true);

        service.addComment(DOCUMENT_ID, "nội dung xấu", true, "Tôi nghĩ đây là nhầm lẫn");

        ArgumentCaptor<Comment> saved = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(saved.capture());
        assertEquals(CommentStatus.PENDING_REVIEW, saved.getValue().getStatus());
        assertEquals("Tôi nghĩ đây là nhầm lẫn", saved.getValue().getDisputeNote());
    }

    @Test
    void disputeWithoutReasonIsRejected() {
        givenKeywordHit(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.addComment(DOCUMENT_ID, "nội dung xấu", true, "   "));
    }

    @Test
    void cleanCommentIsSavedAsVisible() {
        givenKeywordHit(false);

        service.addComment(DOCUMENT_ID, "Tài liệu rất hữu ích", false, null);

        ArgumentCaptor<Comment> saved = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(saved.capture());
        assertEquals(CommentStatus.VISIBLE, saved.getValue().getStatus());
    }

    @Test
    void otherUsersCommentCannotBeEdited() {
        givenExistingComment(OTHER_USER_ID);

        assertThrows(ForbiddenException.class,
                () -> service.updateComment(COMMENT_ID, "sửa trộm", false, null));

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void otherUsersCommentCannotBeDeleted() {
        givenExistingComment(OTHER_USER_ID);

        assertThrows(ForbiddenException.class, () -> service.deleteComment(COMMENT_ID));

        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void authorCanDeleteOwnComment() {
        givenExistingComment(CURRENT_USER_ID);

        service.deleteComment(COMMENT_ID);

        verify(commentRepository).delete(any(Comment.class));
    }
}
