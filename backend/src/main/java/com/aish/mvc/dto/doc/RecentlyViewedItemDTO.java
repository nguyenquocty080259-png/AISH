package com.aish.mvc.dto.doc;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// 1 mục trong "Tiếp tục học". Chỉ chứa doc còn khả dụng nên document luôn có mặt.
@Getter
@Setter
public class RecentlyViewedItemDTO {
    private Long documentId;
    private LocalDateTime viewedAt;
    private DocumentResponseDTO document;
}
