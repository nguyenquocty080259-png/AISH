package com.aish.mvc.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Shape nhẹ cho AiChatResponse.relatedDocs (side-channel gợi ý, tách khỏi câu trả lời
// chính - DEC-027). "title" đặt tên khớp với field đầu tiên mà frontend ChatMessage.jsx
// đã đọc (d?.title || d?.documentTitle || String(d)) nên không cần đổi gì ở frontend.
@Getter
@AllArgsConstructor
public class RelatedDocDTO {
    private Long documentId;
    private String title;
    private String ownerName;
}
