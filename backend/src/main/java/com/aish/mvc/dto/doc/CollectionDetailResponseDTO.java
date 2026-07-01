package com.aish.mvc.dto.doc;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// Chi tiết 1 collection + danh sách item (kèm availability).
@Getter
@Setter
public class CollectionDetailResponseDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CollectionItemDTO> items;
}
