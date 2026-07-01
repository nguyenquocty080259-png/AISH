package com.aish.mvc.dto.doc;

import com.aish.mvc.entity.enums.AppealStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminAppealResponseDTO {
    private Long appealId;
    private DocumentSummaryDTO document;
    private String appellantName;
    private String reason;
    private LocalDateTime createdAt;
    private AppealStatus status;
}
