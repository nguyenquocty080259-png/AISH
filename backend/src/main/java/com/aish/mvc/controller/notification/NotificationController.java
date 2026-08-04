package com.aish.mvc.controller.notification;

import com.aish.mvc.dto.notification.NotificationPreferenceDTO;
import com.aish.mvc.dto.notification.NotificationResponseDTO;
import com.aish.mvc.service.notification.NotificationPreferenceService;
import com.aish.mvc.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * CỬA NGÕ API cho CHUÔNG THÔNG BÁO: danh sách, số chưa đọc, đánh dấu đã đọc, xoá, và cài đặt loại
 * thông báo muốn nhận. Toàn bộ đều thao tác trên user đang đăng nhập.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationPreferenceService notificationPreferenceService;

    // GET /api/notifications/mine — danh sách thông báo của tôi, có phân trang (mặc định 15/trang).
    @GetMapping("/mine")
    public ResponseEntity<Page<NotificationResponseDTO>> getMyNotifications(
            @PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getMyNotifications(pageable));
    }

    // GET /api/notifications/unread-count — số thông báo chưa đọc, hiện số đỏ trên chuông.
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount()));
    }

    // PUT /api/notifications/{id}/read — đánh dấu một thông báo đã đọc.
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    // PUT /api/notifications/read-all — đánh dấu tất cả đã đọc.
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/notifications/clear-all — xoá toàn bộ thông báo của tôi.
    @DeleteMapping("/clear-all")
    public ResponseEntity<Void> clearAllMine() {
        notificationService.clearAllMine();
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/notifications/{id} — xoá một thông báo.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/notifications/preferences — cài đặt loại thông báo đang bật/tắt.
    @GetMapping("/preferences")
    public ResponseEntity<List<NotificationPreferenceDTO>> getMyPreferences() {
        return ResponseEntity.ok(notificationPreferenceService.getMyPreferences());
    }

    // PUT /api/notifications/preferences — cập nhật cài đặt loại thông báo muốn nhận.
    @PutMapping("/preferences")
    public ResponseEntity<Void> updateMyPreferences(@RequestBody List<NotificationPreferenceDTO> prefs) {
        notificationPreferenceService.updateMyPreferences(prefs);
        return ResponseEntity.noContent().build();
    }
}
