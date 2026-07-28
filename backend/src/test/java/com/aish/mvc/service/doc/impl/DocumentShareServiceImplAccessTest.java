package com.aish.mvc.service.doc.impl;

import com.aish.mvc.entity.enums.ShareMode;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.DocumentShareRepository;
import com.aish.mvc.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * hasShareAccess() là hàm mà TẤT CẢ các lối đọc nội dung đều gọi (chi tiết, xem trước, tải,
 * và DocumentAccessPort cho Collections/AI chat), nên mọi thay đổi ở đây lan ra toàn hệ thống.
 * Hai đường được chia sẻ: mời trực tiếp (RESTRICTED) và link-share đang bật (ANYONE_WITH_LINK).
 */
class DocumentShareServiceImplAccessTest {

    private static final Long DOCUMENT_ID = 5L;
    private static final Long USER_ID = 7L;

    private final DocumentShareRepository documentShareRepository = mock(DocumentShareRepository.class);

    private DocumentShareServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DocumentShareServiceImpl(
                documentShareRepository,
                mock(DocDocumentRepository.class),
                mock(AuthAccountRepository.class),
                mock(AuthUserRepository.class),
                mock(NotificationService.class),
                mock(DocumentMapper.class));
    }

    @Test
    void directlySharedUserHasAccess() {
        when(documentShareRepository.existsByDocumentIdAndSharedWithUserId(DOCUMENT_ID, USER_ID))
                .thenReturn(true);

        assertTrue(service.hasShareAccess(DOCUMENT_ID, USER_ID));
    }

    @Test
    void userWithoutInviteFallsBackToLinkShare() {
        when(documentShareRepository.existsByDocumentIdAndSharedWithUserId(DOCUMENT_ID, USER_ID))
                .thenReturn(false);
        when(documentShareRepository.existsByDocumentIdAndShareMode(DOCUMENT_ID, ShareMode.ANYONE_WITH_LINK))
                .thenReturn(true);

        assertTrue(service.hasShareAccess(DOCUMENT_ID, USER_ID));
    }

    @Test
    void userWithNeitherInviteNorLinkHasNoAccess() {
        when(documentShareRepository.existsByDocumentIdAndSharedWithUserId(DOCUMENT_ID, USER_ID))
                .thenReturn(false);
        when(documentShareRepository.existsByDocumentIdAndShareMode(DOCUMENT_ID, ShareMode.ANYONE_WITH_LINK))
                .thenReturn(false);

        assertFalse(service.hasShareAccess(DOCUMENT_ID, USER_ID));
    }

    @Test
    void nullUserIsRejectedWithoutTouchingTheDatabase() {
        assertFalse(service.hasShareAccess(DOCUMENT_ID, null));

        verify(documentShareRepository, never())
                .existsByDocumentIdAndSharedWithUserId(anyLong(), anyLong());
        verify(documentShareRepository, never())
                .existsByDocumentIdAndShareMode(anyLong(), any(ShareMode.class));
    }
}
