package com.aish.mvc.repository.notification;

import com.aish.mvc.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId);

    long countByRecipientUserIdAndIsReadFalse(Long recipientUserId);
}
