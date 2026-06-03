package com.aish.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponseDTO {
    private Long id;
    private String title;
    private String description;

    // Trường này để fix lỗi setStatus nè
    private String status;

    // Trường này để fix lỗi setOwnerName nè
    private String ownerName;

    private String visibility;
    private String fileName;
    private String storageUrl;
    private LocalDateTime createdAt;
}