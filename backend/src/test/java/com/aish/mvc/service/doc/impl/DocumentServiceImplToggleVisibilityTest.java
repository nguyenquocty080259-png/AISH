package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.ai.ModerationDecision;
import com.aish.mvc.dto.ai.ModerationResultDTO;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.service.ai.AiModerationService;
import com.aish.mvc.service.doc.DocumentContentKeywordService;
import com.aish.mvc.service.notification.NotificationService;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * toggleVisibility() sang PUBLIC gọi stampMetadataCheck(), vốn là
 * {@code @Modifying(clearAutomatically = true)} — truy vấn này XOÁ SẠCH persistence context, nên
 * thực thể đã nạp trước đó (và proxy lazy {@code user} của nó) trở thành detached NGAY BÊN TRONG
 * transaction. Map thẳng thực thể cũ sau lời gọi đó sẽ ném LazyInitializationException lúc
 * DocumentMapper chạm doc.getUser().getFullName(), làm endpoint trả 500.
 *
 * <p>Test dựng đúng cái bẫy đó: mapper ném LazyInitializationException nếu nhận lại thực thể CŨ,
 * và chỉ trả DTO khi nhận thực thể được nạp lại sau truy vấn xoá context.
 */
class DocumentServiceImplToggleVisibilityTest {

    private static final String OWNER_EMAIL = "owner@example.com";
    private static final Long OWNER_ID = 89L;
    private static final Long DOCUMENT_ID = 5L;
    private static final String OWNER_NAME = "System Administrator";

    private final DocDocumentRepository docDocumentRepository = mock(DocDocumentRepository.class);
    private final AuthAccountRepository authAccountRepository = mock(AuthAccountRepository.class);
    private final AuthUserRepository authUserRepository = mock(AuthUserRepository.class);
    private final DocumentContentKeywordService documentContentKeywordService =
            mock(DocumentContentKeywordService.class);
    private final AiModerationService aiModerationService = mock(AiModerationService.class);
    private final DocumentMapper documentMapper = mock(DocumentMapper.class);
    private final NotificationService notificationService = mock(NotificationService.class);

    private DocumentServiceImpl service;

    /** Thực thể đã nạp trước khi persistence context bị xoá — sau đó là detached. */
    private DocDocument detachedDocument;
    /** Thực thể nạp lại sau truy vấn xoá context — đây mới là bản dùng được. */
    private DocDocument reloadedDocument;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(OWNER_EMAIL, "n/a"));

        AuthUser owner = new AuthUser();
        owner.setId(OWNER_ID);
        owner.setFullName(OWNER_NAME);
        AuthAccount ownerAccount = new AuthAccount();
        ownerAccount.setUser(owner);
        when(authAccountRepository.findByIdentifier(OWNER_EMAIL)).thenReturn(Optional.of(ownerAccount));

        detachedDocument = document(owner);
        reloadedDocument = document(owner);

        // Lần findById đầu là lúc bắt đầu thao tác; lần sau là nạp lại SAU khi context bị xoá.
        when(docDocumentRepository.findById(DOCUMENT_ID))
                .thenReturn(Optional.of(detachedDocument), Optional.of(reloadedDocument));
        when(docDocumentRepository.save(any(DocDocument.class))).thenReturn(detachedDocument);
        when(documentContentKeywordService.matches(any(DocDocument.class))).thenReturn(false);
        when(aiModerationService.screen(DOCUMENT_ID)).thenReturn(
                new ModerationResultDTO(DOCUMENT_ID, ModerationDecision.PASS, "Nội dung phù hợp"));
        when(authUserRepository.findByRole_RoleNameAndStatus(anyString(), any())).thenReturn(List.of());

        // Chạm vào thực thể detached = đúng lỗi đang gặp trên production.
        when(documentMapper.toResponseDTO(detachedDocument)).thenThrow(new LazyInitializationException(
                "Could not initialize proxy [AuthUser#" + OWNER_ID + "] - no Session"));
        DocumentResponseDTO expected = new DocumentResponseDTO();
        expected.setOwnerName(OWNER_NAME);
        expected.setVisibility(DocumentVisibility.PUBLIC.name());
        when(documentMapper.toResponseDTO(reloadedDocument)).thenReturn(expected);

        service = new DocumentServiceImpl();
        ReflectionTestUtils.setField(service, "docDocumentRepository", docDocumentRepository);
        ReflectionTestUtils.setField(service, "authAccountRepository", authAccountRepository);
        ReflectionTestUtils.setField(service, "authUserRepository", authUserRepository);
        ReflectionTestUtils.setField(service, "documentContentKeywordService", documentContentKeywordService);
        ReflectionTestUtils.setField(service, "aiModerationService", aiModerationService);
        ReflectionTestUtils.setField(service, "documentMapper", documentMapper);
        ReflectionTestUtils.setField(service, "notificationService", notificationService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static DocDocument document(AuthUser owner) {
        return DocDocument.builder()
                .id(DOCUMENT_ID)
                .title("Đề cương ôn tập")
                .user(owner)
                .visibility(DocumentVisibility.PRIVATE)
                .moderationStatus(ModerationStatus.NOT_REQUIRED)
                .build();
    }

    @Test
    void goingPublicReturnsDtoWithOwnerNameInsteadOfFailingOnLazyProxy() {
        DocumentResponseDTO dto = service.toggleVisibility(DOCUMENT_ID);

        assertEquals(OWNER_NAME, dto.getOwnerName());
        assertEquals(DocumentVisibility.PUBLIC.name(), dto.getVisibility());
    }

    @Test
    void documentIsReloadedAfterTheContextClearingStamp() {
        service.toggleVisibility(DOCUMENT_ID);

        // Stamp phải chạy TRƯỚC lần nạp lại, và bản map ra ngoài phải là bản nạp lại.
        verify(docDocumentRepository).stampMetadataCheck(anyLong(), anyString(), any());
        verify(documentMapper).toResponseDTO(reloadedDocument);
    }
}
