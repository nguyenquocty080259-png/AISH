package com.aish.mvc.dto.doc;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// Dùng cho list collection của user (kèm số document mỗi collection).
@Getter
@Setter
public class CollectionResponseDTO {
    private Long id;
    private String name;
    private Long documentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
