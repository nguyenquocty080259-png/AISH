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

/**
 * CỬA NGÕ API của luồng tài liệu — mọi request từ giao diện web về tài liệu đều vào đây trước
 * (đường dẫn /api/documents/**): tải lên, xem chi tiết, sửa, xoá/khôi phục, xem trước, tải về,
 * xin công khai, chia sẻ, bình luận/đánh giá/yêu thích và kháng cáo.
 *
 * <p>Controller chỉ làm 3 việc: nhận tham số từ request, gọi service tương ứng, và bọc kết quả
 * vào HTTP response. Toàn bộ nghiệp vụ và kiểm tra quyền nằm ở tầng service
 * ({@link DocumentService}, {@link EngagementService}, {@link DocumentShareService}...).
 */
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

    // GET /api/documents — danh sách tài liệu của chính user đang đăng nhập ("Tài liệu của tôi").
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
    /**
     * POST /api/documents/upload — TẢI TÀI LIỆU LÊN.
     *
     * <p>Nhận dữ liệu dạng form (multipart) vì có kèm file: tiêu đề, mô tả, danh sách id môn học,
     * nơi lưu và chính file đó. Trả về 201 CREATED kèm thông tin tài liệu vừa tạo.
     * Mọi chốt chặn (tuổi, loại tệp, dung lượng, quota) nằm trong service.
     */
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

    // Endpoint cũ: luôn lưu trên ĐĨA máy chủ. Giữ lại để client phiên bản cũ không bị lỗi.
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

    // Endpoint cũ: luôn lưu trên ĐÁM MÂY Cloudinary. Giữ lại để tương thích ngược.
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

    // Bấm tim: chưa thích thì thêm, đang thích thì bỏ (cùng 1 endpoint).
    @PostMapping("/{id}/favorite")
    public ResponseEntity<Void> toggleFavorite(@PathVariable Long id) {
        engagementService.toggleFavorite(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Thêm bình luận vào tài liệu. Nội dung bị lọc từ khoá cấm trước khi lưu.
     *
     * <p>Trúng từ khoá -> trả 422 kèm lý do để FE hiện hộp thoại "bình luận bị chặn"; người dùng
     * có thể gửi lại với dispute=true + lý do khiếu nại, khi đó bình luận được lưu ở trạng thái
     * chờ Admin duyệt thay vì bị chặn thẳng.
     */
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

    // Sửa bình luận — chỉ tác giả. Nội dung mới bị lọc từ khoá lại y như lúc thêm mới.
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

    // Xoá bình luận — chỉ tác giả của bình luận đó.
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        engagementService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    // Sửa thông tin tài liệu (tiêu đề/mô tả/môn học) — chỉ chủ sở hữu, kiểm tra trong service.
    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> update(@PathVariable Long id, @RequestBody DocumentUpdateRequestDTO request) {
        return ResponseEntity.ok(
                documentService.updateDocument(id, request.getTitle(), request.getDescription(), request.getSubjectIds()));
    }

    // Chấm sao cho tài liệu (1-5). Chấm lại thì ghi đè điểm cũ, không cộng thêm.
    @PostMapping("/{id}/rate")
    public ResponseEntity<Void> rate(@PathVariable Long id, @RequestParam Integer star) {
        engagementService.rateDocument(id, star);
        return ResponseEntity.ok().build();
    }

    // Đưa tài liệu vào THÙNG RÁC (xoá mềm) — vẫn khôi phục được, file trên đĩa còn nguyên.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    // Danh sách tài liệu trong thùng rác của user đang đăng nhập.
    @GetMapping("/trash")
    public ResponseEntity<List<DocumentResponseDTO>> getTrash() {
        return ResponseEntity.ok(documentService.getDeletedDocuments());
    }

    // Danh sách tài liệu user đã bấm yêu thích.
    @GetMapping("/favorites")
    public ResponseEntity<List<DocumentResponseDTO>> getFavorites() {
        return ResponseEntity.ok(documentService.getFavoriteDocuments());
    }

    // Khôi phục tài liệu từ thùng rác về danh sách bình thường.
    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        documentService.restoreDocument(id);
        return ResponseEntity.ok().build();
    }

    // XOÁ VĨNH VIỄN: xoá cả file thật lẫn mọi dữ liệu liên quan. KHÔNG khôi phục được.
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentDelete(@PathVariable Long id) {
        documentService.permanentDeleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    // Trả về document sau khi đổi để FE hiển thị kết quả kiểm duyệt AI
    // (visibility mới, moderationStatus, moderationReason).
    // Đây là nút "Công khai / Ẩn tài liệu": ẩn thì có hiệu lực ngay, còn xin công khai thì phải
    // qua kiểm duyệt AI rồi chờ Admin duyệt cuối (xem DocumentServiceImpl.toggleVisibility).
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

    // Chi tiết 1 tài liệu. Lấy dữ liệu TRƯỚC (đã kiểm tra quyền bên trong), lấy được rồi mới
    // ghi nhận lượt xem — người không có quyền xem thì không bị tính vào lịch sử xem.
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> getOne(@PathVariable Long id) {
        DocumentResponseDTO dto = documentService.getDocumentById(id);
        engagementService.logView(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * TẢI FILE VỀ MÁY.
     *
     * <p>Các bước: (1) lấy bản ghi file kèm kiểm tra quyền, (2) mở nội dung file thật (trên đĩa
     * hoặc trên Cloudinary), (3) ghi nhận lượt tải, (4) trả file kèm header
     * Content-Disposition "attachment" để trình duyệt tải xuống thay vì mở lên.
     */
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

    // XEM TRƯỚC ngay trên trình duyệt: giống /download nhưng dùng header "inline" (mở lên xem)
    // và KHÔNG ghi nhận lượt tải.
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
