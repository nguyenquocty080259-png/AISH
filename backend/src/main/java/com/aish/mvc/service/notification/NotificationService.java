package com.aish.mvc.service.notification;

import com.aish.mvc.dto.notification.NotificationResponseDTO;
import com.aish.mvc.entity.enums.CaseType;
import com.aish.mvc.entity.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void createNotification(Long recipientUserId, NotificationType type, String message, Long relatedReportId);

    void createDocumentNotification(
            Long recipientUserId, NotificationType type, String message, Long relatedDocumentId);

    void notifyCommentUnderReview(Long ownerUserId, Long commentId, Long documentId);

    void notifyCommentReviewed(Long ownerUserId, Long commentId, Long documentId, boolean approved);

    void createCaseNotification(
            Long recipientUserId, NotificationType type, String message, CaseType caseType, Long caseId);

    Page<NotificationResponseDTO> getMyNotifications(Pageable pageable);

    long getUnreadCount();

    NotificationResponseDTO markAsRead(Long notificationId);

    void markAllAsRead();
}
