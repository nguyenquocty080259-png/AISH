package com.aish.mvc.dto.interaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CaseMessageDTO {
    private Long id;
    private Long senderUserId;
    private String senderName;
    private String senderRole;
    private String content;
    private LocalDateTime createdAt;
}
