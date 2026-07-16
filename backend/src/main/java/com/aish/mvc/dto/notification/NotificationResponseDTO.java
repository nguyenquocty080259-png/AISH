package com.aish.mvc.dto.notification;

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
    private Boolean isRead;
    private LocalDateTime createdAt;
}
