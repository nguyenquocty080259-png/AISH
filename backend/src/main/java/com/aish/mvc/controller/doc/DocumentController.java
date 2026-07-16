package com.aish.mvc.controller.doc;

import com.aish.mvc.dto.doc.AppealRequestDTO;
import com.aish.mvc.dto.doc.CommentBlockedResponseDTO;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.dto.doc.ModerationAppealResponseDTO;
import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.exception.CommentBlockedException;
import com.aish.mvc.service.doc.DocumentService;
import com.aish.mvc.service.doc.EngagementService;
import com.aish.mvc.service.doc.ModerationAppealService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.aish.mvc.dto.doc.DocumentUpdateRequestDTO;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final EngagementService engagementService;
    private final ModerationAppealService moderationAppealService;
    private final com.aish.mvc.service.stor.FileResourceResolver fileResourceResolver;

    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> getAll() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    // Endpoint hợp nhất: storage = LOCAL | CLOUD | BOTH (BOTH lưu cả 2 nơi).
    // Giữ 2 endpoint cũ bên dưới để không phá client cũ.
    @PostMapping("/upload")
    public ResponseEntity<DocumentResponseDTO> upload(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "subjectIds", required = false) java.util.List<Long> subjectIds,
            @RequestParam(value = "storage", defaultValue = "LOCAL") String storage,
            @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(
                documentService.uploadDocument(title, description, subjectIds, file, storage),
                HttpStatus.CREATED);
    }

    @PostMapping("/upload-server")
    public ResponseEntity<DocumentResponseDTO> uploadServer(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "subjectIds", required = false) java.util.List<Long> subjectIds,
            @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(
                documentService.uploadDocumentToServer(title, description, subjectIds, file),
                HttpStatus.CREATED);
    }

    @PostMapping("/upload-cloud")
    public ResponseEntity<DocumentResponseDTO> uploadCloud(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "subjectIds", required = false) java.util.List<Long> subjectIds,
            @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(
                documentService.uploadDocumentToCloud(title, description, subjectIds, file),
                HttpStatus.CREATED);
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Void> toggleFavorite(@PathVariable Long id) {
        engagementService.toggleFavorite(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<?> addComment(
            @PathVariable Long id,
            @RequestBody String content,
            @RequestParam(defaultValue = "false") boolean dispute,
            @RequestParam(required = false) String disputeNote) {
        try {
            engagementService.addComment(id, content, dispute, disputeNote);
            return ResponseEntity.ok().build();
        } catch (CommentBlockedException exception) {
            return ResponseEntity.unprocessableEntity()
                    .body(new CommentBlockedResponseDTO(true, exception.getReason()));
        }
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @RequestBody String content,
            @RequestParam(defaultValue = "false") boolean dispute,
            @RequestParam(required = false) String disputeNote) {
        try {
            engagementService.updateComment(commentId, content, dispute, disputeNote);
            return ResponseEntity.ok().build();
        } catch (CommentBlockedException exception) {
            return ResponseEntity.unprocessableEntity()
                    .body(new CommentBlockedResponseDTO(true, exception.getReason()));
        }
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        engagementService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> update(@PathVariable Long id, @RequestBody DocumentUpdateRequestDTO request) {
        return ResponseEntity.ok(
                documentService.updateDocument(id, request.getTitle(), request.getDescription(), request.getSubjectIds()));
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<Void> rate(@PathVariable Long id, @RequestParam Integer star) {
        engagementService.rateDocument(id, star);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trash")
    public ResponseEntity<List<DocumentResponseDTO>> getTrash() {
        return ResponseEntity.ok(documentService.getDeletedDocuments());
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<DocumentResponseDTO>> getFavorites() {
        return ResponseEntity.ok(documentService.getFavoriteDocuments());
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        documentService.restoreDocument(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentDelete(@PathVariable Long id) {
        documentService.permanentDeleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    // Trả về document sau khi đổi để FE hiển thị kết quả kiểm duyệt AI
    // (visibility mới, moderationStatus, moderationReason).
    @PutMapping("/{id}/toggle-visibility")
    public ResponseEntity<DocumentResponseDTO> toggleVisibility(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.toggleVisibility(id));
    }

    // Kháng cáo thủ công sau khi tài liệu bị AI REJECTED — chỉ chủ tài liệu, KHÔNG gọi AI.
    @PostMapping("/{id}/appeal")
    public ResponseEntity<ModerationAppealResponseDTO> appeal(@PathVariable Long id, @RequestBody AppealRequestDTO request) {
        return new ResponseEntity<>(
                moderationAppealService.appeal(id, request.getReason()),
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> getOne(@PathVariable Long id) {
        DocumentResponseDTO dto = documentService.getDocumentById(id);
        engagementService.logView(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        DocFile docFile = documentService.getFileByDocumentId(id);
        try {
            engagementService.logDownload(id);
            Resource resource = resolveResource(docFile);
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + docFile.getFileName() + "\"")
                        .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewFile(@PathVariable Long id) {
        try {
            DocFile docFile = documentService.getFileForPreview(id);
            Resource resource = resolveResource(docFile);
            if (resource.exists() || resource.isReadable()) {
                String contentType = docFile.getFileType() != null ? docFile.getFileType() : "application/octet-stream";
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + docFile.getFileName() + "\"")
                        .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Dùng resolver chung (service/stor/FileResourceResolver) — có fallback signed URL
    // cho file Cloudinary bị chặn deliver public (PDF trên account free).
    private Resource resolveResource(DocFile docFile) {
        return fileResourceResolver.resolve(docFile);
    }
}
