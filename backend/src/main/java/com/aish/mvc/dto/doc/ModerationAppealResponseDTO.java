package com.aish.mvc.dto.doc;

import com.aish.mvc.entity.enums.AppealStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ModerationAppealResponseDTO {
    private Long id;
    private Long documentId;
    private String reason;
    private AppealStatus status;
    private LocalDateTime createdAt;
}
