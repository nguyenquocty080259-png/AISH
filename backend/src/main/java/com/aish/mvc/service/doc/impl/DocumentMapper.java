package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.AdminDocumentSummaryDTO;
import com.aish.mvc.dto.doc.CommentDTO;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.entity.doc.Subject;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.CommentStatus;
import com.aish.mvc.entity.enums.IngestStatus;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.CommentRepository;
import com.aish.mvc.repository.doc.DownloadRepository;
import com.aish.mvc.repository.doc.FavoriteRepository;
import com.aish.mvc.repository.doc.RatingRepository;
import com.aish.mvc.service.doc.DocEmbeddingService;
import com.aish.mvc.service.doc.RoleNames;
import com.aish.mvc.service.doc.StorageTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Dựng DTO tài liệu trả về FE. Không phải mapper thuần: ngoài việc copy trường, nó còn TRUY VẤN
 * thêm số liệu tương tác (yêu thích, lượt tải, điểm trung bình) và danh sách bình luận cho từng
 * tài liệu — nên gọi nó trong vòng lặp là nhân số truy vấn lên theo số tài liệu.
 *
 * <p>Mapper đọc user đang đăng nhập từ SecurityContext để tính các trường phụ thuộc người xem
 * ({@code favorited}, và việc có thấy bình luận đang chờ duyệt hay không), nhưng KHÔNG tự kiểm
 * tra quyền xem tài liệu — người gọi phải chốt quyền trước khi map.
 */
@Component
@RequiredArgsConstructor
public class DocumentMapper {

    private final FavoriteRepository favoriteRepository;
    private final DownloadRepository downloadRepository;
    private final RatingRepository ratingRepository;
    private final CommentRepository commentRepository;
    private final AuthAccountRepository authAccountRepository;
    private final DocEmbeddingService docEmbeddingService;

    private AuthUser currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    /**
     * Đổi {@code DocFile.resourceType} sang nhãn nơi lưu cho FE (DEC-031): "local" (upload lên
     * máy chủ) -> LOCAL; mọi giá trị còn lại đều do Cloudinary đặt ("image", "raw"...) -> CLOUD.
     */
    public String toStorageType(String resourceType) {
        return DocFile.RESOURCE_TYPE_LOCAL.equalsIgnoreCase(resourceType) ? StorageTarget.LOCAL : StorageTarget.CLOUD;
    }

    /**
     * DTO đầy đủ cho trang chi tiết và các danh sách tài liệu: metadata, file chính (ưu tiên bản
     * local), nhãn nơi lưu, thumbnail, môn học, số liệu tương tác và bình luận.
     *
     * <p>Bình luận đang chờ duyệt (PENDING_REVIEW) chỉ hiện với admin hoặc chính tác giả của
     * bình luận đó; người khác không thấy. Tài liệu chưa có file thì {@code aiSupported} là
     * false và các trường file để trống.
     */
    public DocumentResponseDTO toResponseDTO(DocDocument doc) {
        AuthUser currentUser = currentUser();
        Long currentUserId = currentUser.getId();
        boolean isAdmin = currentUser.getRole() != null
                && RoleNames.ADMIN.equals(currentUser.getRole().getRoleName());
        DocumentResponseDTO dto = new DocumentResponseDTO();
        dto.setId(doc.getId());
        dto.setTitle(doc.getTitle());
        dto.setDescription(doc.getDescription());
        dto.setStatus(doc.getStatus() != null ? doc.getStatus().name() : "COMPLETED");
        dto.setVisibility(doc.getVisibility() != null ? doc.getVisibility().name() : "PUBLIC");
        dto.setModerationStatus(doc.getModerationStatus() != null ? doc.getModerationStatus().name() : ModerationStatus.NOT_REQUIRED.name());
        dto.setModerationReason(doc.getModerationReason());
        dto.setAdminReviewedAt(doc.getAdminReviewedAt());
        dto.setIngestStatus(doc.getIngestStatus() != null ? doc.getIngestStatus().name() : IngestStatus.NOT_INGESTED.name());
        dto.setCreatedAt(doc.getCreatedAt());
        dto.setDeletedAt(doc.getDeletedAt());

        if (doc.getUser() != null) dto.setOwnerName(doc.getUser().getFullName());
        if (doc.getFiles() != null && !doc.getFiles().isEmpty()) {
            // Ưu tiên bản local (đọc nhanh, không phụ thuộc Cloudinary) — mode "CẢ HAI" lưu 2 bản.
            DocFile primaryFile = doc.getFiles().stream()
                    .filter(f -> DocFile.RESOURCE_TYPE_LOCAL.equalsIgnoreCase(f.getResourceType()))
                    .findFirst()
                    .orElse(doc.getFiles().getFirst());
            dto.setFileName(primaryFile.getFileName());
            dto.setFileUrl(primaryFile.getFileUrl());
            dto.setFileType(primaryFile.getFileType());

            boolean hasLocal = doc.getFiles().stream().anyMatch(f -> DocFile.RESOURCE_TYPE_LOCAL.equalsIgnoreCase(f.getResourceType()));
            boolean hasCloud = doc.getFiles().stream().anyMatch(f -> !DocFile.RESOURCE_TYPE_LOCAL.equalsIgnoreCase(f.getResourceType()));
            dto.setStorageType(hasLocal && hasCloud ? StorageTarget.BOTH : (hasLocal ? StorageTarget.LOCAL : StorageTarget.CLOUD));

            // Thumbnail: lấy bản đầu tiên có (2 bản của cùng 1 file thì thumbnail giống nhau).
            dto.setThumbnailUrl(doc.getFiles().stream()
                    .map(DocFile::getThumbnailUrl)
                    .filter(t -> t != null && !t.isBlank())
                    .findFirst()
                    .orElse(null));

            dto.setAiSupported(docEmbeddingService.isAiSupported(primaryFile.getFileName(), primaryFile.getFileType()));
        } else {
            dto.setAiSupported(false);
        }
        if (doc.getSubjects() != null && !doc.getSubjects().isEmpty()) {
            dto.setSubjectIds(doc.getSubjects().stream().map(Subject::getId).collect(Collectors.toList()));
            dto.setSubjectNames(doc.getSubjects().stream().map(Subject::getName).collect(Collectors.toList()));
        }

        dto.setFavoriteCount(favoriteRepository.countByDocumentId(doc.getId()));
        dto.setDownloadCount(downloadRepository.countByDocumentId(doc.getId()));
        dto.setAverageRating(ratingRepository.getAverageRatingByDocumentId(doc.getId()));
        dto.setFavorited(favoriteRepository.existsByUserIdAndDocumentId(currentUserId, doc.getId()));

        List<CommentDTO> commentDTOs = commentRepository.findByDocumentIdOrderByCreatedAtDesc(doc.getId()).stream()
                .filter(c -> c.getStatus() == CommentStatus.VISIBLE
                        || (c.getStatus() == CommentStatus.PENDING_REVIEW
                        && (isAdmin || c.getUser().getId().equals(currentUserId))))
                .map(c -> new CommentDTO(
                        c.getId(),
                        c.getUser().getFullName(),
                        c.getContent(),
                        c.getStatus().name(),
                        c.getCreatedAt()))
                .collect(Collectors.toList());
        dto.setComments(commentDTOs);

        return dto;
    }

    /**
     * DTO rút gọn cho bảng quản trị: chỉ metadata và nơi lưu, KHÔNG truy vấn số liệu tương tác
     * hay bình luận — bảng admin phân trang trên toàn bộ tài liệu nên phải giữ rẻ.
     */
    public AdminDocumentSummaryDTO toAdminSummaryDTO(DocDocument doc) {
        String storageType = doc.getFiles() != null && !doc.getFiles().isEmpty()
                ? toStorageType(doc.getFiles().getFirst().getResourceType())
                : null;
        return new AdminDocumentSummaryDTO(
                doc.getId(),
                doc.getTitle(),
                doc.getUser() != null ? doc.getUser().getFullName() : null,
                doc.getVisibility() != null ? doc.getVisibility().name() : null,
                doc.getModerationStatus() != null ? doc.getModerationStatus().name() : ModerationStatus.NOT_REQUIRED.name(),
                storageType,
                doc.getCreatedAt(),
                null,
                doc.getAdminReviewedAt(),
                doc.getDeletedAt());
    }
}
