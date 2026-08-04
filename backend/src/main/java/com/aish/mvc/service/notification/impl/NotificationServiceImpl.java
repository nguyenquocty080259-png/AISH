package com.aish.mvc.service.notification.impl;

import com.aish.mvc.dto.notification.NotificationResponseDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.CaseType;
import com.aish.mvc.entity.enums.NotificationType;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.entity.notification.Notification;
import com.aish.mvc.exception.ForbiddenException;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.notification.NotificationRepository;
import com.aish.mvc.service.notification.NotificationPreferenceService;
import com.aish.mvc.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cài đặt thật của {@link NotificationService}. Việc TẠO thông báo (createNotification,
 * createDocumentNotification, createCaseNotification) chạy NGẦM (@Async) và tự nuốt lỗi — tạo
 * thông báo thất bại không được phép làm hỏng luồng nghiệp vụ chính (vd lưu bình luận vẫn phải
 * thành công dù gửi thông báo lỗi).
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final AuthAccountRepository authAccountRepository;
    private final AuthUserRepository authUserRepository;
    private final NotificationPreferenceService notificationPreferenceService;

    // Lấy user đang đăng nhập từ token.
    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    // Tạo thông báo gắn với report. Chạy ngầm trên luồng riêng; người nhận đã tắt loại thông báo
    // này thì bỏ qua, không tạo. Lỗi bất kỳ chỉ log, không ném ra ngoài.
    @Override
    @Async("notificationExecutor")
    @Transactional
    public void createNotification(
            Long recipientUserId, NotificationType type, String message, Long relatedReportId) {
        try {
            if (!notificationPreferenceService.isEnabled(recipientUserId, type)) return;
            Notification notification = Notification.builder()
                    .recipientUserId(recipientUserId)
                    .type(type)
                    .message(message)
                    .relatedReportId(relatedReportId)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification); // lưu bảng notifications
        } catch (Exception ex) {
            log.error("Failed to create notification for recipientUserId={}, type={}", recipientUserId, type, ex);
        }
    }

    // Tạo thông báo gắn với tài liệu (vd có người bình luận/đánh giá tài liệu của bạn).
    @Override
    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createDocumentNotification(
            Long recipientUserId, NotificationType type, String message, Long relatedDocumentId) {
        try {
            if (!notificationPreferenceService.isEnabled(recipientUserId, type)) return;
            Notification notification = Notification.builder()
                    .recipientUserId(recipientUserId)
                    .type(type)
                    .message(message)
                    .relatedDocumentId(relatedDocumentId)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification); // lưu bảng notifications
        } catch (Exception ex) {
            log.error("Failed to create document notification for recipientUserId={}, type={}", recipientUserId, type, ex);
        }
    }

    // Báo cho CẢ chủ bình luận LẪN mọi Admin đang hoạt động biết có bình luận chờ duyệt (AI vừa
    // gắn cờ) — chủ bình luận nhận thông báo "đang chờ xem xét", Admin nhận thông báo "cần duyệt".
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

    // Báo cho chủ bình luận biết kết quả duyệt của Admin (được duyệt hoặc bị từ chối).
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

    // Tạo thông báo gắn với một "case" (khiếu nại/hỗ trợ).
    @Override
    @Async("notificationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createCaseNotification(
            Long recipientUserId, NotificationType type, String message, CaseType caseType, Long caseId) {
        try {
            if (!notificationPreferenceService.isEnabled(recipientUserId, type)) return;
            Notification notification = Notification.builder()
                    .recipientUserId(recipientUserId)
                    .type(type)
                    .message(message)
                    .relatedCaseType(caseType)
                    .relatedCaseId(caseId)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        } catch (Exception ex) {
            log.error("Failed to create case notification for recipientUserId={}, type={}, caseType={}, caseId={}",
                    recipientUserId, type, caseType, caseId, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getMyNotifications(Pageable pageable) {
        Long userId = getCurrentUser().getId();
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByRecipientUserIdAndIsReadFalse(getCurrentUser().getId());
    }

    // Đánh dấu đã đọc — chặn nếu thông báo không phải của user hiện tại.
    @Override
    @Transactional
    public NotificationResponseDTO markAsRead(Long notificationId) {
        AuthUser currentUser = getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo không tồn tại."));
        if (!notification.getRecipientUserId().equals(currentUser.getId())) {
            throw new ForbiddenException("error.notification.markOthersForbidden");
        }
        notification.setIsRead(true);
        return toResponseDTO(notificationRepository.save(notification)); // lưu bảng notifications
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

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        AuthUser currentUser = getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo không tồn tại."));
        if (!notification.getRecipientUserId().equals(currentUser.getId())) {
            throw new ForbiddenException("error.notification.deleteOthersForbidden");
        }
        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public void clearAllMine() {
        Long userId = getCurrentUser().getId();
        notificationRepository.deleteByRecipientUserId(userId);
    }

    // Chuyển entity Notification sang DTO trả về FE.
    private NotificationResponseDTO toResponseDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .relatedReportId(notification.getRelatedReportId())
                .relatedDocumentId(notification.getRelatedDocumentId())
                .relatedCommentId(notification.getRelatedCommentId())
                .relatedCaseId(notification.getRelatedCaseId())
                .relatedCaseType(notification.getRelatedCaseType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    // Dựng nhanh một thông báo liên quan tới bình luận (dùng chung cho notifyCommentUnderReview/Reviewed).
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
