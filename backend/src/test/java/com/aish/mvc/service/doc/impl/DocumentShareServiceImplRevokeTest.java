package com.aish.mvc.service.doc.impl;

import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.exception.ForbiddenException;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.DocumentShareRepository;
import com.aish.mvc.service.notification.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * revokeShare() gỡ quyền của MỘT người. Chốt quan trọng: chỉ chủ sở hữu gọi được (người đang
 * được chia sẻ không được gỡ quyền của người khác), tài liệu trong thùng rác coi như không tồn
 * tại, và khi từ chối thì không được xoá bản ghi nào.
 */
class DocumentShareServiceImplRevokeTest {

    private static final String CURRENT_USER_EMAIL = "me@example.com";
    private static final Long CURRENT_USER_ID = 7L;
    private static final Long OTHER_USER_ID = 99L;
    private static final Long TARGET_ID = 42L;
    private static final Long DOCUMENT_ID = 5L;

    private final DocumentShareRepository documentShareRepository = mock(DocumentShareRepository.class);
    private final DocDocumentRepository docDocumentRepository = mock(DocDocumentRepository.class);
    private final AuthAccountRepository authAccountRepository = mock(AuthAccountRepository.class);

    private DocumentShareServiceImpl service;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(CURRENT_USER_EMAIL, "n/a"));

        AuthUser currentUser = new AuthUser();
        currentUser.setId(CURRENT_USER_ID);
        AuthAccount authAccount = new AuthAccount();
        authAccount.setUser(currentUser);
        when(authAccountRepository.findByIdentifier(CURRENT_USER_EMAIL)).thenReturn(Optional.of(authAccount));

        service = new DocumentShareServiceImpl(
                documentShareRepository, docDocumentRepository, authAccountRepository,
                mock(AuthUserRepository.class), mock(NotificationService.class), mock(DocumentMapper.class));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void givenDocument(Long ownerId, LocalDateTime deletedAt) {
        AuthUser owner = new AuthUser();
        owner.setId(ownerId);
        when(docDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(DocDocument.builder()
                .id(DOCUMENT_ID)
                .user(owner)
                .visibility(DocumentVisibility.PUBLIC)
                .deletedAt(deletedAt)
                .build()));
    }

    @Test
    void ownerRevokesExactlyOneRecipient() {
        givenDocument(CURRENT_USER_ID, null);

        service.revokeShare(DOCUMENT_ID, TARGET_ID);

        verify(documentShareRepository).deleteByDocumentIdAndSharedWithUserId(DOCUMENT_ID, TARGET_ID);
    }

    @Test
    void nonOwnerCannotRevoke() {
        givenDocument(OTHER_USER_ID, null);

        assertThrows(ForbiddenException.class, () -> service.revokeShare(DOCUMENT_ID, TARGET_ID));

        verify(documentShareRepository, never())
                .deleteByDocumentIdAndSharedWithUserId(anyLong(), anyLong());
    }

    @Test
    void revokingOnSoftDeletedDocumentIsNotFound() {
        givenDocument(CURRENT_USER_ID, LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> service.revokeShare(DOCUMENT_ID, TARGET_ID));

        verify(documentShareRepository, never())
                .deleteByDocumentIdAndSharedWithUserId(anyLong(), anyLong());
    }

    @Test
    void revokingOnUnknownDocumentIsNotFound() {
        when(docDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.revokeShare(DOCUMENT_ID, TARGET_ID));

        verify(documentShareRepository, never())
                .deleteByDocumentIdAndSharedWithUserId(anyLong(), anyLong());
    }
}
