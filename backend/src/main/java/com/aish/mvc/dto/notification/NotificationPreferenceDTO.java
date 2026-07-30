package com.aish.mvc.dto.notification;

import com.aish.mvc.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDTO {
    private NotificationType type;
    private boolean enabled;
}
