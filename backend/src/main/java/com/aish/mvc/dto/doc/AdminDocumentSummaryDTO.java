package com.aish.mvc.dto.doc;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// GET /api/admin/documents — bất kỳ owner/visibility nào, KHÔNG phải DTO owner-scoped.
@Getter
@AllArgsConstructor
public class AdminDocumentSummaryDTO {
    private Long id;
    private String title;
    private String ownerName;
    private String visibility;
    private String moderationStatus;
    private String storageType; // LOCAL | CLOUD — chuẩn hóa từ DocFile.resourceType (DEC-031)
    private LocalDateTime createdAt;
}
