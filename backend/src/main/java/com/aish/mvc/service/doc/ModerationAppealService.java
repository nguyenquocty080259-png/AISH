package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.ModerationAppealResponseDTO;

public interface ModerationAppealService {

    // Chỉ chủ tài liệu mới appeal được, và chỉ khi moderationStatus == REJECTED.
    // Đi thẳng vào hàng chờ Admin (APPEAL_PENDING) — KHÔNG gọi AI.
    ModerationAppealResponseDTO appeal(Long documentId, String reason);
}
