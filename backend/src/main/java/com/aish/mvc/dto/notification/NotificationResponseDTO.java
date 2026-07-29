package com.aish.mvc.dto.notification;

import com.aish.mvc.entity.enums.CaseType;
import com.aish.mvc.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {

    private Long id;
    private NotificationType type;
    private String message;
    private Long relatedReportId;
    private Long relatedDocumentId;
    private Long relatedCommentId;
    private Long relatedCaseId;
    private CaseType relatedCaseType;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
