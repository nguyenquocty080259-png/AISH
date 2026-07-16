package com.aish.mvc.service.notification.impl;

import com.aish.mvc.dto.notification.NotificationResponseDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.NotificationType;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.entity.notification.Notification;
import com.aish.mvc.exception.ForbiddenException;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.notification.NotificationRepository;
import com.aish.mvc.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthAccountRepository authAccountRepository;
    private final AuthUserRepository authUserRepository;

    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    @Override
    @Transactional
    public void createNotification(
            Long recipientUserId, NotificationType type, String message, Long relatedReportId) {
        Notification notification = Notification.builder()
                .recipientUserId(recipientUserId)
                .type(type)
                .message(message)
                .relatedReportId(relatedReportId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createDocumentNotification(
            Long recipientUserId, NotificationType type, String message, Long relatedDocumentId) {
        Notification notification = Notification.builder()
                .recipientUserId(recipientUserId)
                .type(type)
                .message(message)
                .relatedDocumentId(relatedDocumentId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyCommentUnderReview(Long ownerUserId, Long commentId, Long documentId) {
        notificationRepository.save(commentNotification(
                ownerUserId,
                NotificationType.COMMENT_UNDER_REVIEW,
                "Bình luận của bạn đang chờ quản trị viên xem xét.",
                commentId,
                documentId));

        authUserRepository.findByRole_RoleNameAndStatus("ADMIN", UserStatus.ACTIVE)
                .forEach(admin -> notificationRepository.save(commentNotification(
                        admin.getId(),
                        NotificationType.COMMENT_UNDER_REVIEW,
                        "Có bình luận mới cần kiểm duyệt.",
                        commentId,
                        documentId)));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyCommentReviewed(
            Long ownerUserId, Long commentId, Long documentId, boolean approved) {
        String message = approved
                ? "Bình luận của bạn đã được duyệt và hiển thị công khai."
                : "Bình luận của bạn đã bị từ chối sau khi xem xét.";
        notificationRepository.save(commentNotification(
                ownerUserId, NotificationType.COMMENT_REVIEWED, message, commentId, documentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getMyNotifications() {
        Long userId = getCurrentUser().getId();
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByRecipientUserIdAndIsReadFalse(getCurrentUser().getId());
    }

    @Override
    @Transactional
    public NotificationResponseDTO markAsRead(Long notificationId) {
        AuthUser currentUser = getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo không tồn tại."));
        if (!notification.getRecipientUserId().equals(currentUser.getId())) {
            throw new ForbiddenException("Bạn không có quyền đánh dấu thông báo của người khác là đã đọc.");
        }
        notification.setIsRead(true);
        return toResponseDTO(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        Long userId = getCurrentUser().getId();
        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(notification -> !Boolean.TRUE.equals(notification.getIsRead()))
                .toList();
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    private NotificationResponseDTO toResponseDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .relatedReportId(notification.getRelatedReportId())
                .relatedDocumentId(notification.getRelatedDocumentId())
                .relatedCommentId(notification.getRelatedCommentId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private Notification commentNotification(
            Long recipientUserId,
            NotificationType type,
            String message,
            Long commentId,
            Long documentId) {
        return Notification.builder()
                .recipientUserId(recipientUserId)
                .type(type)
                .message(message)
                .relatedCommentId(commentId)
                .relatedDocumentId(documentId)
                .isRead(false)
                .build();
    }
}
