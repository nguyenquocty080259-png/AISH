package com.aish.mvc.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AiMessageDTO {
    private Long id;
    private String role;
    private String content;
    private Integer orderIndex;
    private LocalDateTime createdAt;
}
