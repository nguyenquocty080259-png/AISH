package com.aish.mvc.service.doc.impl;

import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.Comment;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.Favorite;
import com.aish.mvc.entity.doc.Rating;
import com.aish.mvc.exception.ForbiddenException;
import com.aish.mvc.exception.ResourceNotFoundException;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Bình luận / đánh giá / yêu thích là thao tác TRÊN nội dung tài liệu, nên chỉ hợp lệ khi người
 * dùng thật sự được xem tài liệu đó. Trước đây các luồng này chỉ kiểm tra tài liệu có tồn tại
 * (hoặc không kiểm tra gì), nên chỉ cần đoán id là tương tác được với tài liệu PRIVATE của
 * người khác. Luật quyền lấy từ DocumentAccessPort, ở đây chỉ khoá việc CÓ hỏi cổng đó hay không.
 */
class EngagementServiceImplAccessTest {

    private static final String CURRENT_USER_EMAIL = "me@example.com";
    private static final Long CURRENT_USER_ID = 7L;
    private static final Long DOCUMENT_ID = 3L;

    private final DocDocumentRepository docDocumentRepository = mock(DocDocumentRepository.class);
    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final RatingRepository ratingRepository = mock(RatingRepository.class);
    private final FavoriteRepository favoriteRepository = mock(FavoriteRepository.class);
    private final DocumentAccessPort documentAccessPort = mock(DocumentAccessPort.class);
    private final ToxicKeywordFilter toxicKeywordFilter = mock(ToxicKeywordFilter.class);
    private final AuthAccountRepository authAccountRepository = mock(AuthAccountRepository.class);

    private EngagementServiceImpl service;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(CURRENT_USER_EMAIL, "n/a"));

        AuthUser authUser = new AuthUser();
        authUser.setId(CURRENT_USER_ID);
        AuthAccount authAccount = new AuthAccount();
        authAccount.setUser(authUser);
        when(authAccountRepository.findByIdentifier(CURRENT_USER_EMAIL)).thenReturn(Optional.of(authAccount));
        when(docDocumentRepository.findById(DOCUMENT_ID))
                .thenReturn(Optional.of(DocDocument.builder().id(DOCUMENT_ID).build()));

        service = new EngagementServiceImpl(
                docDocumentRepository,
                commentRepository,
                favoriteRepository,
                ratingRepository,
                mock(DownloadRepository.class),
                mock(ViewHistoryRepository.class),
                authAccountRepository,
                toxicKeywordFilter,
                mock(NotificationService.class),
                mock(ApplicationEventPublisher.class),
                documentAccessPort);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void commentOnDocumentWithoutReadAccessIsForbidden() {
        when(documentAccessPort.isAvailableTo(DOCUMENT_ID, CURRENT_USER_ID)).thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> service.addComment(DOCUMENT_ID, "Tài liệu hữu ích", false, null));

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void ratingDocumentWithoutReadAccessIsForbidden() {
        when(documentAccessPort.isAvailableTo(DOCUMENT_ID, CURRENT_USER_ID)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> service.rateDocument(DOCUMENT_ID, 5));

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void ratingIsSavedWhenDocumentIsReadable() {
        when(documentAccessPort.isAvailableTo(DOCUMENT_ID, CURRENT_USER_ID)).thenReturn(true);
        when(ratingRepository.findByUserIdAndDocument_Id(CURRENT_USER_ID, DOCUMENT_ID))
                .thenReturn(Optional.empty());

        service.rateDocument(DOCUMENT_ID, 5);

        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void favoritingDocumentWithoutReadAccessIsForbidden() {
        when(documentAccessPort.isAvailableTo(DOCUMENT_ID, CURRENT_USER_ID)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> service.toggleFavorite(DOCUMENT_ID));

        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    void favoritingUnknownDocumentIsNotFound() {
        // Trước đây không kiểm tra gì: id không tồn tại vẫn tạo được bản ghi favorite mồ côi.
        when(docDocumentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.toggleFavorite(404L));

        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    void favoritingDocumentNotYetFavoritedCreatesTheRecord() {
        when(documentAccessPort.isAvailableTo(DOCUMENT_ID, CURRENT_USER_ID)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndDocumentId(CURRENT_USER_ID, DOCUMENT_ID))
                .thenReturn(false);

        service.toggleFavorite(DOCUMENT_ID);

        verify(favoriteRepository).save(any(Favorite.class));
        verify(favoriteRepository, never()).deleteByUserIdAndDocumentId(CURRENT_USER_ID, DOCUMENT_ID);
    }

    @Test
    void favoritingAgainRemovesTheRecord() {
        // Cùng một endpoint bật/tắt: đã yêu thích rồi thì gọi lại là bỏ yêu thích, không tạo
        // thêm bản ghi thứ hai.
        when(documentAccessPort.isAvailableTo(DOCUMENT_ID, CURRENT_USER_ID)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndDocumentId(CURRENT_USER_ID, DOCUMENT_ID))
                .thenReturn(true);

        service.toggleFavorite(DOCUMENT_ID);

        verify(favoriteRepository).deleteByUserIdAndDocumentId(CURRENT_USER_ID, DOCUMENT_ID);
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    void commentIsSavedWhenDocumentIsReadable() {
        when(documentAccessPort.isAvailableTo(DOCUMENT_ID, CURRENT_USER_ID)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenAnswer(call -> call.getArgument(0));

        service.addComment(DOCUMENT_ID, "Tài liệu hữu ích", false, null);

        verify(commentRepository).save(any(Comment.class));
    }
}
