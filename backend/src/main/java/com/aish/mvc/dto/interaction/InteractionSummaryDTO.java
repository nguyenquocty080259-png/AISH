package com.aish.mvc.dto.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class InteractionSummaryDTO {
    private long unreadNotifications;
    private long myPendingReports;
    private long myPendingAppeals;
    private long adminPendingReports;
    private long adminPendingAppeals;
}
