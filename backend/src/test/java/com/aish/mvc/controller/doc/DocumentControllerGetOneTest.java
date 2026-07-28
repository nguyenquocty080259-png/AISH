package com.aish.mvc.controller.doc;

import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.exception.ForbiddenException;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.service.doc.DocumentService;
import com.aish.mvc.service.doc.DocumentShareService;
import com.aish.mvc.service.doc.DocumentTextExtractor;
import com.aish.mvc.service.doc.EngagementService;
import com.aish.mvc.service.doc.ModerationAppealService;
import com.aish.mvc.service.stor.FileResourceResolver;
import com.aish.mvc.service.stor.UploadFileTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * getOne() chỉ được ghi nhận lượt xem SAU khi DocumentService.getDocumentById() qua được kiểm
 * tra quyền — nếu không, lượt xem của một request bị từ chối (403) hoặc tài liệu đã xoá (404)
 * vẫn chảy vào lịch sử xem và số liệu thống kê. Thứ tự này dễ bị đảo ngược khi refactor, nên
 * khoá lại bằng test.
 */
class DocumentControllerGetOneTest {

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

    @Test
    void doesNotLogViewWhenAccessIsDenied() {
        when(documentService.getDocumentById(9L))
                .thenThrow(new ForbiddenException("Bạn không có quyền xem tài liệu này!"));

        assertThrows(ForbiddenException.class, () -> controller.getOne(9L));

        verify(engagementService, never()).logView(anyLong());
    }

    @Test
    void doesNotLogViewForDeletedDocument() {
        when(documentService.getDocumentById(8L))
                .thenThrow(new ResourceNotFoundException("Tài liệu không tồn tại!"));

        assertThrows(ResourceNotFoundException.class, () -> controller.getOne(8L));

        verify(engagementService, never()).logView(anyLong());
    }

    @Test
    void logsViewAfterAllowedRead() {
        DocumentResponseDTO dto = new DocumentResponseDTO();
        when(documentService.getDocumentById(1L)).thenReturn(dto);

        ResponseEntity<DocumentResponseDTO> response = controller.getOne(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
        verify(engagementService).logView(1L);
    }
}
