package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.exception.ForbiddenException;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.service.doc.NamingModerationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * updateDocument() sửa metadata tài liệu. Hai chốt phải giữ: chỉ chủ sở hữu, và tài liệu đang
 * ở thùng rác coi như không tồn tại — nếu không, tài liệu đã xoá mềm vẫn đổi được tiêu đề
 * trong khi mọi lối đọc đều trả 404 cho nó.
 */
class DocumentServiceImplUpdateTest {

    private static final String CURRENT_USER_EMAIL = "me@example.com";
    private static final Long CURRENT_USER_ID = 7L;
    private static final Long OTHER_USER_ID = 99L;
    private static final Long DOCUMENT_ID = 5L;

    private final DocDocumentRepository docDocumentRepository = mock(DocDocumentRepository.class);
    private final AuthAccountRepository authAccountRepository = mock(AuthAccountRepository.class);
    private final NamingModerationService namingModerationService = mock(NamingModerationService.class);
    private final DocumentMapper documentMapper = mock(DocumentMapper.class);

    private DocumentServiceImpl service;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(CURRENT_USER_EMAIL, "n/a"));

        AuthUser authUser = new AuthUser();
        authUser.setId(CURRENT_USER_ID);
        AuthAccount authAccount = new AuthAccount();
        authAccount.setUser(authUser);
        when(authAccountRepository.findByIdentifier(CURRENT_USER_EMAIL)).thenReturn(Optional.of(authAccount));
        when(namingModerationService.validate(anyString())).thenAnswer(call -> call.getArgument(0));

        service = new DocumentServiceImpl();
        ReflectionTestUtils.setField(service, "docDocumentRepository", docDocumentRepository);
        ReflectionTestUtils.setField(service, "authAccountRepository", authAccountRepository);
        ReflectionTestUtils.setField(service, "namingModerationService", namingModerationService);
        ReflectionTestUtils.setField(service, "documentMapper", documentMapper);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private DocDocument givenDocument(Long ownerId, LocalDateTime deletedAt) {
        AuthUser owner = new AuthUser();
        owner.setId(ownerId);
        DocDocument doc = DocDocument.builder()
                .id(DOCUMENT_ID)
                .title("Tiêu đề cũ")
                .user(owner)
                .deletedAt(deletedAt)
                .build();
        when(docDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));
        return doc;
    }

    @Test
    void softDeletedDocumentCannotBeUpdated() {
        givenDocument(CURRENT_USER_ID, LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateDocument(DOCUMENT_ID, "Tiêu đề mới", "mô tả", null));

        verify(docDocumentRepository, never()).save(any(DocDocument.class));
    }

    @Test
    void otherUsersDocumentCannotBeUpdated() {
        givenDocument(OTHER_USER_ID, null);

        assertThrows(ForbiddenException.class,
                () -> service.updateDocument(DOCUMENT_ID, "Tiêu đề mới", "mô tả", null));

        verify(docDocumentRepository, never()).save(any(DocDocument.class));
    }

    @Test
    void ownerCanUpdateTitleAndDescription() {
        DocDocument doc = givenDocument(CURRENT_USER_ID, null);
        when(docDocumentRepository.save(doc)).thenReturn(doc);
        when(documentMapper.toResponseDTO(doc)).thenReturn(new DocumentResponseDTO());

        service.updateDocument(DOCUMENT_ID, "Tiêu đề mới", "mô tả mới", null);

        assertEquals("Tiêu đề mới", doc.getTitle());
        assertEquals("mô tả mới", doc.getDescription());
    }
}
