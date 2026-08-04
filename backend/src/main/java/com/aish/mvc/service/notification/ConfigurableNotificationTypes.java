package com.aish.mvc.service.notification;

import com.aish.mvc.entity.enums.NotificationType;

import java.util.Set;

/**
 * Chỉ 4 loại thông báo "xã hội" (ít quan trọng, dễ gây phiền) cho phép người dùng tự tắt/bật
 * trong phần cài đặt thông báo; các loại còn lại (vd kết quả kiểm duyệt) luôn bật, không tắt được.
 */
public final class ConfigurableNotificationTypes {

    public static final Set<NotificationType> CONFIGURABLE = Set.of(
            NotificationType.COMMENT_ON_MY_DOC,
            NotificationType.RATING_ON_MY_DOC,
            NotificationType.DOCUMENT_SHARED,
            NotificationType.CASE_REPLY
    );

    private ConfigurableNotificationTypes() {
    }
}
