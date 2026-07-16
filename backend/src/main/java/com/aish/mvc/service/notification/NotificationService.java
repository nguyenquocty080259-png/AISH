package com.aish.mvc.service.notification;

import com.aish.mvc.dto.notification.NotificationResponseDTO;
import com.aish.mvc.entity.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    void createNotification(Long recipientUserId, NotificationType type, String message, Long relatedReportId);

    void createDocumentNotification(
            Long recipientUserId, NotificationType type, String message, Long relatedDocumentId);

    List<NotificationResponseDTO> getMyNotifications();

    long getUnreadCount();

    NotificationResponseDTO markAsRead(Long notificationId);

    void markAllAsRead();
}
