package com.aish.mvc.service.notification;

import com.aish.mvc.dto.notification.NotificationPreferenceDTO;
import com.aish.mvc.entity.enums.NotificationType;

import java.util.List;

/**
 * Cài đặt "muốn nhận loại thông báo nào" của từng người dùng — chỉ áp dụng cho 4 loại trong
 * {@link ConfigurableNotificationTypes}.
 */
public interface NotificationPreferenceService {

    // Trạng thái 4 loại CONFIGURABLE cho current user; loại chưa có row -> enabled=true.
    List<NotificationPreferenceDTO> getMyPreferences();

    // Upsert cho current user; chỉ chấp nhận loại thuộc CONFIGURABLE, loại khác bị bỏ qua.
    void updateMyPreferences(List<NotificationPreferenceDTO> prefs);

    // Loại ngoài CONFIGURABLE -> luôn true (không tra DB). Loại trong CONFIGURABLE
    // không có row -> mặc định true (bật).
    boolean isEnabled(Long userId, NotificationType type);
}
