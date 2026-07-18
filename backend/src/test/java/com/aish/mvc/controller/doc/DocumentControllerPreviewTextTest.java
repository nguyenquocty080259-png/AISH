package com.aish.mvc.controller.doc;

import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.exception.ForbiddenException;
import com.aish.mvc.service.doc.DocumentService;
import com.aish.mvc.service.doc.DocumentTextExtractor;
import com.aish.mvc.service.doc.EngagementService;
import com.aish.mvc.service.doc.ModerationAppealService;
import com.aish.mvc.service.stor.FileResourceResolver;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * previewText() (A3/T4) phải dùng CHUNG DocumentService.getFileForPreview() nên hành vi từ
 * chối truy cập phải giống hệt /preview (RuntimeException -> 403 — xem previewFile() cùng
 * class), và trả 204 khi extractor không trích được gì (file hỏng/có mật khẩu/rỗng) thay vì
 * lỗi 500 hay trang trắng.
 */
class DocumentControllerPreviewTextTest {

    private final DocumentService documentService = mock(DocumentService.class);
    private final EngagementService engagementService = mock(EngagementService.class);
    private final ModerationAppealService moderationAppealService = mock(ModerationAppealService.class);
    private final FileResourceResolver fileResourceResolver = mock(FileResourceResolver.class);
    private final DocumentTextExtractor documentTextExtractor = mock(DocumentTextExtractor.class);

    private final DocumentController controller = new DocumentController(
            documentService, engagementService, moderationAppealService,
            fileResourceResolver, documentTextExtractor);

    @Test
    void deniesAccessExactlyLikePreview() {
        when(documentService.getFileForPreview(99L))
                .thenThrow(new ForbiddenException("Bạn không có quyền xem trước tài liệu này!"));

        ResponseEntity<String> response = controller.previewText(99L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void returnsNoContentWhenNothingExtracted() {
        DocFile docFile = DocFile.builder().fileName("corrupt.docx").build();
        Resource resource = mock(Resource.class);
        when(documentService.getFileForPreview(1L)).thenReturn(docFile);
        when(fileResourceResolver.resolve(docFile)).thenReturn(resource);
        when(documentTextExtractor.extract(resource, docFile)).thenReturn(null);

        ResponseEntity<String> response = controller.previewText(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void returnsExtractedTextAsPlainText() {
        DocFile docFile = DocFile.builder().fileName("slides.pptx").build();
        Resource resource = mock(Resource.class);
        when(documentService.getFileForPreview(2L)).thenReturn(docFile);
        when(fileResourceResolver.resolve(docFile)).thenReturn(resource);
        when(documentTextExtractor.extract(resource, docFile)).thenReturn("— Slide 1 —\nNội dung");

        ResponseEntity<String> response = controller.previewText(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("— Slide 1 —\nNội dung", response.getBody());
    }
}
