package com.aish.mvc.service.doc;

/**
 * Sự kiện "có bình luận cần AI kiểm duyệt". EngagementServiceImpl bắn ra sau khi lưu bình luận,
 * {@link CommentModerationService} lắng nghe và xử lý ngầm — nhờ vậy người dùng không phải chờ AI.
 * Chỉ mang id bình luận; bên nhận tự nạp lại dữ liệu mới nhất từ database.
 */
public record CommentModerationRequestedEvent(Long commentId) {
}
