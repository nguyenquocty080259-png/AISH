package com.aish.mvc.dto.doc;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Tóm tắt tài liệu cho các danh sách admin (queue kháng cáo...) — nhẹ, tránh N+1
// so với DocumentResponseDTO đầy đủ (favorites/comments/rating...).
@Getter
@AllArgsConstructor
public class DocumentSummaryDTO {
    private Long id;
    private String title;
    private String ownerName;
    private String moderationReason;
}
