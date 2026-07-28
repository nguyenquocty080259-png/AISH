package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.ShareRequestDTO;
import com.aish.mvc.dto.doc.ShareResponseDTO;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.DocumentShare;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.ShareMode;
import com.aish.mvc.entity.enums.SharePermission;
import com.aish.mvc.entity.enums.UserStatus;
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
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * shareDocument() là nơi tập trung mọi luật chia sẻ: chỉ chủ sở hữu, tài liệu phải còn sống và
 * đang PUBLIC, EDITOR chưa hỗ trợ, không tự chia sẻ cho mình, không chia sẻ cho tài khoản bị
 * khoá, và chia sẻ lại cho cùng một người phải CẬP NHẬT bản ghi cũ chứ không tạo dòng trùng.
 * Trước đây không có test nào cho service này.
 */
class DocumentShareServiceImplShareTest {

    private static final String OWNER_EMAIL = "owner@example.com";
    private static final Long OWNER_ID = 7L;
    private static final Long TARGET_ID = 42L;
    private static final Long DOCUMENT_ID = 5L;
    private static final String TARGET_EMAIL = "ban@example.com";

    private final DocumentShareRepository documentShareRepository = mock(DocumentShareRepository.class);
    private final DocDocumentRepository docDocumentRepository = mock(DocDocumentRepository.class);
    private final AuthAccountRepository authAccountRepository = mock(AuthAccountRepository.class);
    private final AuthUserRepository authUserRepository = mock(AuthUserRepository.class);
    private final NotificationService notificationService = mock(NotificationService.class);

    private DocumentShareServiceImpl service;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(OWNER_EMAIL, "n/a"));
        when(authAccountRepository.findByIdentifier(OWNER_EMAIL))
                .thenReturn(Optional.of(account(user(OWNER_ID, UserStatus.ACTIVE), OWNER_EMAIL)));

        service = new DocumentShareServiceImpl(
                documentShareRepository, docDocumentRepository, authAccountRepository,
                authUserRepository, notificationService, mock(DocumentMapper.class));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static AuthUser user(Long id, UserStatus status) {
        AuthUser authUser = new AuthUser();
        authUser.setId(id);
        authUser.setFullName("Người dùng " + id);
        authUser.setStatus(status);
        return authUser;
    }

    private static AuthAccount account(AuthUser owner, String identifier) {
        AuthAccount authAccount = new AuthAccount();
        authAccount.setUser(owner);
        authAccount.setIdentifier(identifier);
        return authAccount;
    }

    private void givenDocument(Long ownerId, DocumentVisibility visibility, LocalDateTime deletedAt) {
        DocDocument doc = DocDocument.builder()
                .id(DOCUMENT_ID)
                .title("Đề cương")
                .user(user(ownerId, UserStatus.ACTIVE))
                .visibility(visibility)
                .deletedAt(deletedAt)
                .build();
        when(docDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));
    }

    private void givenTarget(Long id, UserStatus status) {
        when(authAccountRepository.findByIdentifierIgnoreCase(TARGET_EMAIL))
                .thenReturn(List.of(account(user(id, status), TARGET_EMAIL)));
    }

    private static ShareRequestDTO request(ShareMode mode, String email, SharePermission permission) {
        ShareRequestDTO dto = new ShareRequestDTO();
        dto.setMode(mode);
        dto.setEmail(email);
        dto.setPermission(permission);
        return dto;
    }

    @Test
    void onlyOwnerCanShare() {
        givenDocument(999L, DocumentVisibility.PUBLIC, null);

        assertThrows(ForbiddenException.class, () -> service.shareDocument(
                DOCUMENT_ID, request(ShareMode.RESTRICTED, TARGET_EMAIL, null)));
    }

    @Test
    void softDeletedDocumentCannotBeShared() {
        givenDocument(OWNER_ID, DocumentVisibility.PUBLIC, LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> service.shareDocument(
                DOCUMENT_ID, request(ShareMode.RESTRICTED, TARGET_EMAIL, null)));
    }

    @Test
    void missingModeIsRejected() {
        givenDocument(OWNER_ID, DocumentVisibility.PUBLIC, null);

        assertThrows(IllegalArgumentException.class, () -> service.shareDocument(
                DOCUMENT_ID, request(null, TARGET_EMAIL, null)));
    }

    @Test
    void editorPermissionIsNotSupportedYet() {
        givenDocument(OWNER_ID, DocumentVisibility.PUBLIC, null);

        assertThrows(IllegalArgumentException.class, () -> service.shareDocument(
                DOCUMENT_ID, request(ShareMode.RESTRICTED, TARGET_EMAIL, SharePermission.EDITOR)));
    }

    @Test
    void onlyPublicDocumentsCanBeShared() {
        givenDocument(OWNER_ID, DocumentVisibility.PRIVATE, null);

        assertThrows(IllegalArgumentException.class, () -> service.shareDocument(
                DOCUMENT_ID, request(ShareMode.RESTRICTED, TARGET_EMAIL, null)));
    }

    @Test
    void sharingWithYourselfIsRejected() {
        givenDocument(OWNER_ID, DocumentVisibility.PUBLIC, null);
        givenTarget(OWNER_ID, UserStatus.ACTIVE);

        assertThrows(IllegalArgumentException.class, () -> service.shareDocument(
                DOCUMENT_ID, request(ShareMode.RESTRICTED, TARGET_EMAIL, null)));
    }

    @Test
    void sharingWithBannedAccountIsRejected() {
        givenDocument(OWNER_ID, DocumentVisibility.PUBLIC, null);
        givenTarget(TARGET_ID, UserStatus.BANNED);

        assertThrows(IllegalArgumentException.class, () -> service.shareDocument(
                DOCUMENT_ID, request(ShareMode.RESTRICTED, TARGET_EMAIL, null)));
    }

    @Test
    void unknownEmailIsNotFound() {
        givenDocument(OWNER_ID, DocumentVisibility.PUBLIC, null);
        when(authAccountRepository.findByIdentifierIgnoreCase(TARGET_EMAIL)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> service.shareDocument(
                DOCUMENT_ID, request(ShareMode.RESTRICTED, TARGET_EMAIL, null)));
    }

    @Test
    void emailMatchingSeveralAccountsIsRefusedInsteadOfGuessing() {
        givenDocument(OWNER_ID, DocumentVisibility.PUBLIC, null);
        when(authAccountRepository.findByIdentifierIgnoreCase(TARGET_EMAIL)).thenReturn(List.of(
                account(user(TARGET_ID, UserStatus.ACTIVE), TARGET_EMAIL),
                account(user(43L, UserStatus.ACTIVE), TARGET_EMAIL)));

        assertThrows(IllegalStateException.class, () -> service.shareDocument(
                DOCUMENT_ID, request(ShareMode.RESTRICTED, TARGET_EMAIL, null)));
    }

    @Test
    void sharingAgainWithTheSamePersonUpdatesTheExistingRow() {
        givenDocument(OWNER_ID, DocumentVisibility.PUBLIC, null);
        givenTarget(TARGET_ID, UserStatus.ACTIVE);
        DocumentShare existing = DocumentShare.builder()
                .id(1L)
                .documentId(DOCUMENT_ID)
                .sharedWithUserId(TARGET_ID)
                .sharedByUserId(OWNER_ID)
                .permission(SharePermission.VIEWER)
                .build();
        when(documentShareRepository.findByDocumentIdAndSharedWithUserId(DOCUMENT_ID, TARGET_ID))
                .thenReturn(Optional.of(existing));

        service.shareDocument(DOCUMENT_ID, request(ShareMode.RESTRICTED, TARGET_EMAIL, SharePermission.COMMENTER));

        ArgumentCaptor<DocumentShare> saved = ArgumentCaptor.forClass(DocumentShare.class);
        verify(documentShareRepository).save(saved.capture());
        assertEquals(1L, saved.getValue().getId());
        assertEquals(SharePermission.COMMENTER, saved.getValue().getPermission());
    }

    @Test
    void enablingLinkShareCreatesAToken() {
        givenDocument(OWNER_ID, DocumentVisibility.PUBLIC, null);
        when(documentShareRepository.findByDocumentId(DOCUMENT_ID)).thenReturn(List.of());

        ShareResponseDTO response = service.shareDocument(
                DOCUMENT_ID, request(ShareMode.ANYONE_WITH_LINK, null, null));

        assertEquals(ShareMode.ANYONE_WITH_LINK.name(), response.getShareMode());
        assertNotNull(response.getShareToken());
    }

    @Test
    void disablingLinkShareRemovesTheLinkRow() {
        givenDocument(OWNER_ID, DocumentVisibility.PRIVATE, null);

        ShareResponseDTO response = service.shareDocument(
                DOCUMENT_ID, request(ShareMode.NONE, null, null));

        // Tắt link-share là thu hồi nên không bị gác điều kiện PUBLIC.
        assertEquals(ShareMode.NONE.name(), response.getShareMode());
        verify(documentShareRepository)
                .deleteByDocumentIdAndShareMode(DOCUMENT_ID, ShareMode.ANYONE_WITH_LINK);
        verify(documentShareRepository, never()).save(any(DocumentShare.class));
    }
}
