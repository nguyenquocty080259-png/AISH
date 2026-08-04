package com.aish.mvc.service.notification;

import com.aish.mvc.dto.notification.NotificationResponseDTO;
import com.aish.mvc.entity.enums.CaseType;
import com.aish.mvc.entity.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Tạo và quản lý THÔNG BÁO trong hệ thống (chuông thông báo trên giao diện): bình luận/report bị
 * xử lý, tài liệu được chia sẻ, bình luận bị AI gắn cờ chờ duyệt... Trước khi tạo thông báo loại
 * "xã hội", nơi gọi nên tự kiểm tra {@link NotificationPreferenceService#isEnabled} để tôn trọng
 * cài đặt tắt/bật của người nhận.
 */
public interface NotificationService {

    // Tạo thông báo liên quan tới một report (vd report của bạn đã được xử lý).
    void createNotification(Long recipientUserId, NotificationType type, String message, Long relatedReportId);

    // Tạo thông báo liên quan tới một tài liệu (vd có người bình luận/đánh giá tài liệu của bạn).
    void createDocumentNotification(
            Long recipientUserId, NotificationType type, String message, Long relatedDocumentId);

    // Báo cho chủ bình luận biết bình luận của họ đang chờ Admin duyệt (do AI gắn cờ).
    void notifyCommentUnderReview(Long ownerUserId, Long commentId, Long documentId);

    // Báo cho chủ bình luận biết kết quả duyệt (approved = true/false).
    void notifyCommentReviewed(Long ownerUserId, Long commentId, Long documentId, boolean approved);

    // Tạo thông báo liên quan tới một "case" (khiếu nại/hỗ trợ) theo loại case cụ thể.
    void createCaseNotification(
            Long recipientUserId, NotificationType type, String message, CaseType caseType, Long caseId);

    // Danh sách thông báo của user hiện tại, có phân trang.
    Page<NotificationResponseDTO> getMyNotifications(Pageable pageable);

    // Số thông báo CHƯA ĐỌC — hiện số đỏ trên biểu tượng chuông.
    long getUnreadCount();

    // Đánh dấu một thông báo đã đọc.
    NotificationResponseDTO markAsRead(Long notificationId);

    // Đánh dấu TẤT CẢ thông báo của user hiện tại là đã đọc.
    void markAllAsRead();

    // Xoá một thông báo.
    void deleteNotification(Long notificationId);

    // Xoá TOÀN BỘ thông báo của user hiện tại.
    void clearAllMine();
}
