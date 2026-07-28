package com.aish.mvc.service.doc.impl;

import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.DocFile;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Hai lối lấy file — getFileByDocumentId() (/download) và getFileForPreview() (/preview,
 * /thumbnail, /preview-text) — phải cùng luật truy cập và cùng cách xử lý tài liệu chưa có
 * file (404, không phải 500). Chúng cũng phải cùng ưu tiên bản LOCAL khi tài liệu lưu ở
 * chế độ "CẢ HAI", vì bản Cloudinary có thể bị chặn deliver.
 */
class DocumentServiceImplFileAccessTest {

    private static final String CURRENT_USER_EMAIL = "me@example.com";
    private static final Long CURRENT_USER_ID = 7L;
    private static final Long OTHER_USER_ID = 99L;
    private static final Long DOCUMENT_ID = 5L;

    private final DocDocumentRepository docDocumentRepository = mock(DocDocumentRepository.class);
    private final AuthAccountRepository authAccountRepository = mock(AuthAccountRepository.class);
    private final DocumentShareService documentShareService = mock(DocumentShareService.class);

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

        service = new DocumentServiceImpl();
        ReflectionTestUtils.setField(service, "docDocumentRepository", docDocumentRepository);
        ReflectionTestUtils.setField(service, "authAccountRepository", authAccountRepository);
        ReflectionTestUtils.setField(service, "documentShareService", documentShareService);
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

    private DocDocument givenDocument(Long ownerId, DocumentVisibility visibility, DocFile... files) {
        DocDocument doc = DocDocument.builder()
                .id(DOCUMENT_ID)
                .user(user(ownerId))
                .visibility(visibility)
                .build();
        for (DocFile file : files) {
            doc.addFile(file);
        }
        when(docDocumentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(doc));
        return doc;
    }

    private static DocFile file(String resourceType, String fileName) {
        return DocFile.builder().resourceType(resourceType).fileName(fileName).build();
    }

    @Test
    void downloadOfDocumentWithoutFilesIsNotFound() {
        // Tài liệu tồn tại nhưng chưa gắn file (upload hỏng giữa chừng) -> 404 rõ ràng,
        // không phải NoSuchElementException rơi thành 500.
        givenDocument(CURRENT_USER_ID, DocumentVisibility.PRIVATE);

        assertThrows(ResourceNotFoundException.class, () -> service.getFileByDocumentId(DOCUMENT_ID));
    }

    @Test
    void previewOfDocumentWithoutFilesIsNotFound() {
        givenDocument(CURRENT_USER_ID, DocumentVisibility.PRIVATE);

        assertThrows(ResourceNotFoundException.class, () -> service.getFileForPreview(DOCUMENT_ID));
    }

    @Test
    void downloadOfOtherUsersPrivateDocumentIsForbidden() {
        givenDocument(OTHER_USER_ID, DocumentVisibility.PRIVATE, file("local", "rieng-tu.pdf"));
        when(documentShareService.hasShareAccess(DOCUMENT_ID, CURRENT_USER_ID)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> service.getFileByDocumentId(DOCUMENT_ID));
    }

    @Test
    void downloadOfSharedDocumentIsAllowedForRecipient() {
        givenDocument(OTHER_USER_ID, DocumentVisibility.SHARED, file("local", "duoc-chia-se.pdf"));
        when(documentShareService.hasShareAccess(DOCUMENT_ID, CURRENT_USER_ID)).thenReturn(true);

        assertEquals("duoc-chia-se.pdf", service.getFileByDocumentId(DOCUMENT_ID).getFileName());
    }

    @Test
    void downloadOfPublicDocumentIsAllowedForAnyUser() {
        givenDocument(OTHER_USER_ID, DocumentVisibility.PUBLIC, file("local", "cong-khai.pdf"));

        assertEquals("cong-khai.pdf", service.getFileByDocumentId(DOCUMENT_ID).getFileName());
    }

    @Test
    void prefersLocalCopyWhenDocumentIsStoredInBothPlaces() {
        // Chế độ "CẢ HAI": bản Cloudinary được thêm sau nhưng bản local phải luôn thắng —
        // đọc nhanh hơn và không dính hạn chế deliver PDF của Cloudinary.
        givenDocument(CURRENT_USER_ID, DocumentVisibility.PRIVATE,
                file("raw", "ban-cloud.pdf"), file("local", "ban-local.pdf"));

        assertEquals("ban-local.pdf", service.getFileByDocumentId(DOCUMENT_ID).getFileName());
        assertEquals("ban-local.pdf", service.getFileForPreview(DOCUMENT_ID).getFileName());
    }

    @Test
    void fallsBackToCloudCopyWhenThereIsNoLocalFile() {
        givenDocument(CURRENT_USER_ID, DocumentVisibility.PRIVATE, file("raw", "chi-co-cloud.pdf"));

        assertEquals("chi-co-cloud.pdf", service.getFileByDocumentId(DOCUMENT_ID).getFileName());
    }
}
