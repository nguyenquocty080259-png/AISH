package com.aish.mvc.dto.doc;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DocumentResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String visibility;
    private String ownerName;
    private String fileName;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    private Long favoriteCount;
    private Long downloadCount;
    private Double averageRating;

    private Boolean favorited;

    private List<CommentDTO> comments;
    private java.util.List<Long> subjectIds;
    private java.util.List<String> subjectNames;
    private String fileUrl;
    private String fileType;
    private String storageType; // LOCAL | CLOUD | BOTH — chuẩn hóa từ danh sách DocFile
    private String thumbnailUrl; // tương đối trong /uploads (vd. "thumbnails/x.png") | null -> FE dùng icon // LOCAL | CLOUD — chuẩn hóa từ DocFile.resourceType

    // DEC-035: kết quả kiểm duyệt AI cho lần chuyển PUBLIC gần nhất.
    private String moderationStatus; // NOT_REQUIRED | APPROVED | REJECTED
    private String moderationReason;
    private LocalDateTime adminReviewedAt;

    private String ingestStatus; // NOT_INGESTED | INGESTED | UNSUPPORTED_FORMAT
    private Boolean aiSupported;
}
