package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.ModerationAppealResponseDTO;

import java.util.List;

public interface ModerationAppealService {

    // Chỉ chủ tài liệu mới appeal được, và chỉ khi moderationStatus == REJECTED.
    // Đi thẳng vào hàng chờ Admin (APPEAL_PENDING) — KHÔNG gọi AI.
    ModerationAppealResponseDTO appeal(Long documentId, String reason);

    // Kháng cáo của user hiện tại (mới nhất trước).
    List<ModerationAppealResponseDTO> listMyAppeals();
}
