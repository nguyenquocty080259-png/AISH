package com.aish.mvc.service.interaction;

import com.aish.mvc.dto.interaction.InteractionSummaryDTO;

public interface InteractionSummaryService {

    // Đếm tổng hợp cho badge các tab của Interaction Hub — role-aware (admin có thêm 2 field admin).
    InteractionSummaryDTO getSummary();
}
