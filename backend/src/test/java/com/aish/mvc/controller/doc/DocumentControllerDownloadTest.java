package com.aish.mvc.controller.doc;

import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.service.doc.DocumentService;
import com.aish.mvc.service.doc.DocumentShareService;
import com.aish.mvc.service.doc.DocumentTextExtractor;
import com.aish.mvc.service.doc.EngagementService;
import com.aish.mvc.service.doc.ModerationAppealService;
import com.aish.mvc.service.stor.FileResourceResolver;
import com.aish.mvc.service.stor.UploadFileTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * downloadFile() chỉ được cộng lượt tải khi thật sự trả file về. File đã mất trên đĩa (hoặc
 * Cloudinary chặn deliver) phải cho ra 404 mà KHÔNG chạm vào downloadCount — nếu không, số liệu
 * tài liệu và bảng xếp hạng "tải nhiều nhất" bị thổi lên bởi những lần tải hỏng.
 * Điều kiện "đọc được" là exists() VÀ isReadable(), không phải HOẶC.
 */
class DocumentControllerDownloadTest {

    private static final Long DOCUMENT_ID = 3L;

    private final DocumentService documentService = mock(DocumentService.class);
    private final EngagementService engagementService = mock(EngagementService.class);
    private final ModerationAppealService moderationAppealService = mock(ModerationAppealService.class);
    private final DocumentShareService documentShareService = mock(DocumentShareService.class);
    private final FileResourceResolver fileResourceResolver = mock(FileResourceResolver.class);
    private final DocumentTextExtractor documentTextExtractor = mock(DocumentTextExtractor.class);
    private final UploadFileTypeService uploadFileTypeService = mock(UploadFileTypeService.class);

    private final DocumentController controller = new DocumentController(
            documentService, engagementService, moderationAppealService, documentShareService,
            fileResourceResolver, documentTextExtractor, uploadFileTypeService);

    private Resource givenResource(boolean exists, boolean readable) {
        DocFile docFile = DocFile.builder().fileName("de-cuong.pdf").fileType("application/pdf").build();
        Resource resource = mock(Resource.class);
        when(documentService.getFileByDocumentId(DOCUMENT_ID)).thenReturn(docFile);
        when(fileResourceResolver.resolve(docFile)).thenReturn(resource);
        when(resource.exists()).thenReturn(exists);
        when(resource.isReadable()).thenReturn(readable);
        return resource;
    }

    @Test
    void doesNotLogDownloadWhenFileIsMissingOnDisk() {
        givenResource(false, true);

        ResponseEntity<Resource> response = controller.downloadFile(DOCUMENT_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(engagementService, never()).logDownload(anyLong());
    }

    @Test
    void doesNotLogDownloadWhenFileIsNotReadable() {
        givenResource(true, false);

        ResponseEntity<Resource> response = controller.downloadFile(DOCUMENT_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(engagementService, never()).logDownload(anyLong());
    }

    @Test
    void logsDownloadOnceWhenFileIsReturned() {
        Resource resource = givenResource(true, true);

        ResponseEntity<Resource> response = controller.downloadFile(DOCUMENT_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resource, response.getBody());
        verify(engagementService).logDownload(DOCUMENT_ID);
    }
}
