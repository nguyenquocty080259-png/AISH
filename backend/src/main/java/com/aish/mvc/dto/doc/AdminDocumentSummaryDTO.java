package com.aish.mvc.dto.doc;

import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.ModerationStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// GET /api/admin/documents — bất kỳ owner/visibility nào, KHÔNG phải DTO owner-scoped.
@Getter
@Setter
@AllArgsConstructor
public class AdminDocumentSummaryDTO {
    private Long id;
    private String title;
    private String ownerName;
    private String visibility;
    private String moderationStatus;
    private String storageType; // LOCAL | CLOUD — chuẩn hóa từ DocFile.resourceType (DEC-031)
    private LocalDateTime createdAt;
    private String ingestStatus;
    private LocalDateTime adminReviewedAt;
    private LocalDateTime deletedAt; // null = đang hoạt động; khác null = đã bị gỡ (soft-delete)

    public AdminDocumentSummaryDTO(Long id, String title, String ownerName, String visibility,
                                   String moderationStatus, String storageType, LocalDateTime createdAt) {
        this(id, title, ownerName, visibility, moderationStatus, storageType, createdAt, null, null, null);
    }

    public AdminDocumentSummaryDTO(Long id, String title, String ownerName, String visibility,
                                   String moderationStatus, String storageType, LocalDateTime createdAt,
                                   String ingestStatus) {
        this(id, title, ownerName, visibility, moderationStatus, storageType, createdAt, ingestStatus, null, null);
    }
}
