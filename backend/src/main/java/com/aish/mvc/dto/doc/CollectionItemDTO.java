package com.aish.mvc.dto.doc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// 1 item trong collection.
// - available = false: CHỈ lộ { documentId, available, addedAt } (document = null bị Jackson bỏ đi).
//   Gộp "đã xóa" và "chủ chuyển private" thành 1 trạng thái, KHÔNG lộ title/owner/lý do.
// - available = true : kèm DTO document đầy đủ (tái dùng DocumentResponseDTO).
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CollectionItemDTO {
    private Long documentId;
    private Boolean available;
    private LocalDateTime addedAt;
    private DocumentResponseDTO document;
}
