package com.aish.mvc.service.notification;

import com.aish.mvc.entity.enums.NotificationType;

import java.util.Set;

// Chỉ 4 loại "xã hội" cho phép user tắt/bật; các loại còn lại luôn bật.
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
