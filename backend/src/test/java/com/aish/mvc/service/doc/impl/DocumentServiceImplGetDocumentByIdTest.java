package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.exception.ForbiddenException;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.service.doc.DocumentShareService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * getDocumentById() phải áp CÙNG luật truy cập với getFileForPreview()/getFileByDocumentId():
 * chủ sở hữu / PUBLIC / được chia sẻ. Chi tiết tài liệu cũng là nội dung (mô tả, tên tệp, bình
 * luận), nên nếu thiếu chốt này thì bất kỳ user đã đăng nhập nào cũng đọc được tài liệu PRIVATE
 * của người khác chỉ bằng cách đoán id.
 */
class DocumentServiceImplGetDocumentByIdTest {

    private static final String CURRENT_USER_EMAIL = "me@example.com";
    private static final Long CURRENT_USER_ID = 7L;
    private static final Long OTHER_USER_ID = 99L;
    private static final Long DOCUMENT_ID = 5L;

    private final DocDocumentRepository docDocumentRepository = mock(DocDocumentRepository.class);
    private final AuthAccountRepository authAccountRepository = mock(AuthAccountRepository.class);
    private final DocumentShareService documentShareService = mock(DocumentShareService.class);
    private final DocumentMapper documentMapper = mock(DocumentMapper.class);

    private DocumentServiceImpl service;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(CURRENT_USER_EMAIL, "n/a"));
        when(authAccountRepository.findByIdentifier(CURRENT_USER_EMAIL))
                .thenReturn(Optional.of(account(CURRENT_USER_ID)));

        service = new DocumentServiceImpl();
        ReflectionTestUtils.setField(service, "docDocumentRepository", docDocumentRepository);
        ReflectionTestUtils.setField(service, "authAccountRepository", authAccountRepository);
        ReflectionTestUtils.setField(service, "documentShareService", documentShareService);
        ReflectionTestUtils.setField(service, "documentMapper", documentMapper);
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

    private static AuthAccount account(Long userId) {
        AuthAccount authAccount = new AuthAccount();
        authAccount.setUser(user(userId));
        return authAccount;
    }

    private DocDocument givenDocument(Long ownerId, DocumentVisibility visibility) {
        DocDocument doc = DocDocument.builder()
                .id(DOCUMENT_ID)
                .title("Đề cương ôn tập")
                .user(user(ownerId))
                .visibility(visibility)
                .build();
        when(docDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));
        return doc;
    }

    @Test
    void otherUsersPrivateDocumentIsForbidden() {
        givenDocument(OTHER_USER_ID, DocumentVisibility.PRIVATE);
        when(documentShareService.hasShareAccess(DOCUMENT_ID, CURRENT_USER_ID)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> service.getDocumentById(DOCUMENT_ID));
    }

    @Test
    void forbiddenDocumentIsNeverMappedToDto() {
        // Chặn phải xảy ra TRƯỚC khi map — nếu không, nội dung vẫn bị đọc ra (và bình luận vẫn
        // bị truy vấn) rồi mới ném lỗi, chỉ là rò rỉ chậm hơn.
        givenDocument(OTHER_USER_ID, DocumentVisibility.PRIVATE);
        when(documentShareService.hasShareAccess(DOCUMENT_ID, CURRENT_USER_ID)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> service.getDocumentById(DOCUMENT_ID));

        verify(documentMapper, never()).toResponseDTO(any(DocDocument.class));
    }

    @Test
    void softDeletedDocumentIsNotFoundEvenForOwner() {
        // Trong thùng rác -> 404 cho MỌI người: chủ sở hữu khôi phục qua /restore chứ không
        // mở lại trang chi tiết, nên không có luồng hợp lệ nào cần đọc tài liệu đã xoá mềm.
        DocDocument doc = givenDocument(CURRENT_USER_ID, DocumentVisibility.PRIVATE);
        doc.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> service.getDocumentById(DOCUMENT_ID));
    }

    @Test
    void softDeletedPublicDocumentIsNotFound() {
        // PUBLIC + đã xoá mềm: check "đã xoá" phải chạy TRƯỚC check quyền, nếu không tài liệu
        // vừa bị gỡ vẫn đọc được bình thường.
        DocDocument doc = givenDocument(OTHER_USER_ID, DocumentVisibility.PUBLIC);
        doc.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> service.getDocumentById(DOCUMENT_ID));
        verify(documentMapper, never()).toResponseDTO(any(DocDocument.class));
    }

    @Test
    void ownerCanReadOwnPrivateDocument() {
        DocDocument doc = givenDocument(CURRENT_USER_ID, DocumentVisibility.PRIVATE);
        DocumentResponseDTO expected = new DocumentResponseDTO();
        when(documentMapper.toResponseDTO(doc)).thenReturn(expected);

        assertSame(expected, service.getDocumentById(DOCUMENT_ID));
    }

    @Test
    void publicDocumentIsReadableByAnyLoggedInUser() {
        DocDocument doc = givenDocument(OTHER_USER_ID, DocumentVisibility.PUBLIC);
        DocumentResponseDTO expected = new DocumentResponseDTO();
        when(documentMapper.toResponseDTO(doc)).thenReturn(expected);

        assertSame(expected, service.getDocumentById(DOCUMENT_ID));
    }

    @Test
    void sharedDocumentIsReadableByRecipient() {
        DocDocument doc = givenDocument(OTHER_USER_ID, DocumentVisibility.SHARED);
        when(documentShareService.hasShareAccess(DOCUMENT_ID, CURRENT_USER_ID)).thenReturn(true);
        DocumentResponseDTO expected = new DocumentResponseDTO();
        when(documentMapper.toResponseDTO(doc)).thenReturn(expected);

        assertSame(expected, service.getDocumentById(DOCUMENT_ID));
    }

    @Test
    void sharedDocumentThatWasRevokedIsForbidden() {
        // Tài liệu từng ở chế độ SHARED nhưng người này đã bị gỡ quyền -> không còn đọc được.
        givenDocument(OTHER_USER_ID, DocumentVisibility.SHARED);
        when(documentShareService.hasShareAccess(DOCUMENT_ID, CURRENT_USER_ID)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> service.getDocumentById(DOCUMENT_ID));
    }
}
