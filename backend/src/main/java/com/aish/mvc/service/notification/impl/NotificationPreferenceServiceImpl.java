package com.aish.mvc.service.notification.impl;

import com.aish.mvc.dto.notification.NotificationPreferenceDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.NotificationType;
import com.aish.mvc.entity.notification.NotificationPreference;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.notification.NotificationPreferenceRepository;
import com.aish.mvc.service.notification.ConfigurableNotificationTypes;
import com.aish.mvc.service.notification.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Cài đặt thật của {@link NotificationPreferenceService}. */
@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final AuthAccountRepository authAccountRepository;

    // Lấy user đang đăng nhập từ token.
    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    // Lấy trạng thái bật/tắt của 4 loại thông báo có thể cấu hình. Loại chưa có bản ghi trong DB
    // (user chưa từng đổi) mặc định coi là ĐANG BẬT.
    @Override
    @Transactional(readOnly = true)
    public List<NotificationPreferenceDTO> getMyPreferences() {
        Long userId = getCurrentUser().getId();
        Map<NotificationType, Boolean> existing = notificationPreferenceRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(NotificationPreference::getType, NotificationPreference::isEnabled));

        return ConfigurableNotificationTypes.CONFIGURABLE.stream()
                .map(type -> new NotificationPreferenceDTO(type, existing.getOrDefault(type, true)))
                .toList();
    }

    // Cập nhật cài đặt: chỉ áp dụng cho các loại nằm trong CONFIGURABLE, loại khác bị bỏ qua âm
    // thầm (không báo lỗi). Chưa có bản ghi thì tạo mới, có rồi thì cập nhật (upsert).
    @Override
    @Transactional
    public void updateMyPreferences(List<NotificationPreferenceDTO> prefs) {
        Long userId = getCurrentUser().getId();
        for (NotificationPreferenceDTO pref : prefs) {
            if (!ConfigurableNotificationTypes.CONFIGURABLE.contains(pref.getType())) {
                continue;
            }
            NotificationPreference entity = notificationPreferenceRepository
                    .findByUserIdAndType(userId, pref.getType())
                    .orElseGet(() -> NotificationPreference.builder()
                            .userId(userId)
                            .type(pref.getType())
                            .build());
            entity.setEnabled(pref.isEnabled());
            notificationPreferenceRepository.save(entity); // lưu bảng notification_preferences
        }
    }

    // Dùng NỘI BỘ (NotificationService gọi trước khi tạo thông báo): loại không thể cấu hình luôn
    // trả true (luôn gửi); loại có thể cấu hình mà chưa có bản ghi cũng mặc định true.
    @Override
    @Transactional(readOnly = true)
    public boolean isEnabled(Long userId, NotificationType type) {
        if (!ConfigurableNotificationTypes.CONFIGURABLE.contains(type)) {
            return true;
        }
        return notificationPreferenceRepository.findByUserIdAndType(userId, type)
                .map(NotificationPreference::isEnabled)
                .orElse(true);
    }
}
