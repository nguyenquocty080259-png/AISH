package com.aish.mvc.service.admin;

import com.aish.mvc.dto.doc.AdminAppealResponseDTO;
import com.aish.mvc.dto.doc.AdminStatsDTO;
import com.aish.mvc.entity.enums.AppealStatus;

import java.util.List;

public interface AdminService {

    // status == null -> mọi appeal (mọi trạng thái).
    List<AdminAppealResponseDTO> listAppeals(AppealStatus status);

    // Chỉ xử lý được appeal đang APPEAL_PENDING. Approve: appeal -> APPEAL_APPROVED,
    // document -> PUBLIC/APPROVED.
    AdminAppealResponseDTO approveAppeal(Long appealId, String adminNote);

    // Reject: appeal -> APPEAL_REJECTED, document giữ nguyên PRIVATE/REJECTED.
    AdminAppealResponseDTO rejectAppeal(Long appealId, String adminNote);

    AdminStatsDTO getStats();
}
