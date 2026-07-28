package com.aish.mvc.controller.doc;

import com.aish.mvc.dto.doc.AppealRequestDTO;
import com.aish.mvc.dto.doc.CommentBlockedResponseDTO;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.dto.doc.ModerationAppealResponseDTO;
import com.aish.mvc.dto.doc.DocumentShareRecipientDTO;
import com.aish.mvc.dto.doc.ShareRequestDTO;
import com.aish.mvc.dto.doc.ShareResponseDTO;
import com.aish.mvc.dto.doc.SharedWithMeItemDTO;
import com.aish.mvc.dto.doc.StorageUsageDTO;
import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.exception.CommentBlockedException;
import com.aish.mvc.exception.ForbiddenException;
import com.aish.mvc.service.doc.DocumentService;
import com.aish.mvc.service.doc.DocumentShareService;
import com.aish.mvc.service.doc.DocumentTextExtractor;
import com.aish.mvc.service.doc.EngagementService;
import com.aish.mvc.service.doc.ModerationAppealService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.aish.mvc.dto.doc.DocumentUpdateRequestDTO;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private static final String THUMB_SUBDIR = "thumbnails";

    private final DocumentService documentService;
    private final EngagementService engagementService;
    private final ModerationAppealService moderationAppealService;
    private final DocumentShareService documentShareService;
    private final com.aish.mvc.service.stor.FileResourceResolver fileResourceResolver;
    private final DocumentTextExtractor documentTextExtractor;
    private final com.aish.mvc.service.stor.UploadFileTypeService uploadFileTypeService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> getAll() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    // Đọc cho bất kỳ user đã đăng nhập nào (anyRequest().authenticated() trong SecurityConfig) -
    // form upload FE dùng để tự chặn trước khi gửi file lớn/vượt quota, và để hiển thị thanh
    // dung lượng (My Documents, Hồ sơ). Luôn tính theo user của token, không nhận userId từ client.
    @GetMapping("/storage-usage")
    public ResponseEntity<StorageUsageDTO> getStorageUsage() {
        return ResponseEntity.ok(documentService.getStorageUsage());
    }

    // Đọc cho mọi user đã đăng nhập - form upload FE dùng để set thuộc tính accept và tiền-kiểm
    // đuôi tệp phía client. BE (DocumentServiceImpl -> UploadFileTypeService) vẫn là chốt chặn cuối.
    @GetMapping("/allowed-file-types")
    public ResponseEntity<com.aish.mvc.dto.config.UploadFileTypesDTO> getAllowedFileTypes() {
        return ResponseEntity.ok(
                new com.aish.mvc.dto.config.UploadFileTypesDTO(uploadFileTypeService.getAllowedExtensions()));
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

    // Chia sẻ tài liệu — chỉ chủ sở hữu. mode = RESTRICTED (userIds) | ANYONE_WITH_LINK (token) | NONE.
    @PostMapping("/{id}/share")
    public ResponseEntity<ShareResponseDTO> share(@PathVariable Long id, @RequestBody ShareRequestDTO request) {
        return ResponseEntity.ok(documentShareService.shareDocument(id, request));
    }

    // Danh sách tài liệu được chia sẻ với người dùng hiện tại.
    @GetMapping("/shared-with-me")
    public ResponseEntity<List<SharedWithMeItemDTO>> sharedWithMe() {
        return ResponseEntity.ok(documentShareService.listSharedWithMe());
    }

    // Danh sách người đang được chia sẻ tài liệu này — CHỈ chủ sở hữu gọi được.
    @GetMapping("/{id}/shares")
    public ResponseEntity<List<DocumentShareRecipientDTO>> shareRecipients(@PathVariable Long id) {
        return ResponseEntity.ok(documentShareService.listShareRecipients(id));
    }

    // Gỡ quyền chia sẻ của 1 user cụ thể — chỉ chủ sở hữu.
    @DeleteMapping("/{id}/share/{userId}")
    public ResponseEntity<Void> revokeShare(@PathVariable Long id, @PathVariable Long userId) {
        documentShareService.revokeShare(id, userId);
        return ResponseEntity.noContent().build();
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
            Resource resource = resolveResource(docFile);
            if (resource.exists() && resource.isReadable()) {
                // Ghi nhận lượt tải SAU khi chắc chắn có file thật để trả về: trước đây log
                // chạy trước nên cả request kết thúc bằng 404 (file mất trên đĩa) hay 500 vẫn
                // cộng vào downloadCount — số liệu và bảng xếp hạng "tải nhiều nhất" bị thổi lên.
                engagementService.logDownload(id);
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
            if (resource.exists() && resource.isReadable()) {
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

    // Ảnh thumbnail do BE sinh lúc upload — dùng CHUNG getFileForPreview() nên quyền truy cập
    // giống hệt /preview (owner hoặc PUBLIC, sai quyền -> 403 qua ForbiddenException, tài liệu/
    // file không tồn tại -> 404 qua ResourceNotFoundException ném thẳng lên GlobalExceptionHandler).
    // 204 khi tài liệu không có thumbnail (docx/pptx/txt, hoặc sinh thumbnail lúc upload thất bại)
    // thay vì ảnh vỡ — FE coi đó là tín hiệu chuyển sang nhánh fallback (preview gốc / icon).
    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<Resource> thumbnail(@PathVariable Long id) {
        DocFile docFile;
        try {
            docFile = documentService.getFileForPreview(id);
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String thumbnailUrl = docFile.getThumbnailUrl();
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            return ResponseEntity.noContent().build();
        }

        try {
            Path thumbDir = Paths.get(uploadDir).resolve(THUMB_SUBDIR).normalize();
            Path path = Paths.get(uploadDir).resolve(thumbnailUrl).normalize();
            // Chống path traversal: chỉ đọc file nằm TRONG uploads/thumbnails (giống
            // ThumbnailServiceImpl.deleteThumbnail).
            if (!path.startsWith(thumbDir)) {
                return ResponseEntity.noContent().build();
            }

            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "image/png")
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"")
                        .body(resource);
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }

    // Text trích xuất best-effort cho DOCX/PPTX/khác — dùng CHUNG getFileForPreview() nên
    // quyền truy cập giống hệt /preview (owner hoặc PUBLIC). Không đọc file trực tiếp từ đĩa:
    // đi qua fileResourceResolver như mọi nhánh khác. 204 khi không trích xuất được (file rỗng,
    // hỏng, hoặc có mật khẩu) thay vì lỗi 500 — FE coi đó là tín hiệu chuyển sang nhánh tải file.
    @GetMapping("/{id}/preview-text")
    public ResponseEntity<String> previewText(@PathVariable Long id) {
        try {
            DocFile docFile = documentService.getFileForPreview(id);
            Resource resource = resolveResource(docFile);
            String text = documentTextExtractor.extract(resource, docFile);
            if (text == null || text.isBlank()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok()
                    .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                    .body(text);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Dùng resolver chung (service/stor/FileResourceResolver) — có fallback signed URL
    // cho file Cloudinary bị chặn deliver public (PDF trên account free).
    private Resource resolveResource(DocFile docFile) {
        return fileResourceResolver.resolve(docFile);
    }
}
