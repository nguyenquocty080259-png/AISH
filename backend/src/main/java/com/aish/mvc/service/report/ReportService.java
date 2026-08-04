package com.aish.mvc.service.report;

import com.aish.mvc.dto.report.AdminReportResponseDTO;
import com.aish.mvc.dto.report.ReportResponseDTO;
import com.aish.mvc.entity.enums.ReportStatus;
import com.aish.mvc.entity.enums.ReportTargetType;

import java.util.List;

/**
 * BÁO CÁO VI PHẠM: người dùng tự báo cáo (tài liệu, bình luận, tin nhắn AI...) và hệ thống tự
 * tạo report khi AI phát hiện vi phạm (createSystemReport, không cần người dùng bấm nút).
 * Admin xem và xử lý report ở trang quản trị.
 */
public interface ReportService {

    // Người dùng gửi báo cáo mới. targetType: loại đối tượng bị báo cáo (tài liệu/bình luận/...).
    ReportResponseDTO createReport(String targetType, Long targetId, String reason);

    // Danh sách report mà user hiện tại đã gửi.
    List<ReportResponseDTO> getMyReports();

    // (Admin) Danh sách report theo trạng thái xử lý; status = null thì lấy tất cả.
    List<AdminReportResponseDTO> listReports(ReportStatus status);

    // (Admin) Xử lý một report: ghi lại hành động đã thực hiện + phản hồi cho người báo cáo.
    AdminReportResponseDTO resolveReport(Long reportId, String actionTakenRaw, String adminResponse);

    // Hệ thống TỰ TẠO report (không phải người dùng bấm nút) — dùng khi AI kiểm duyệt phát hiện
    // vi phạm ở nơi chưa có cơ chế báo cáo thủ công (vd tin nhắn chat AI).
    void createSystemReport(ReportTargetType targetType, Long targetId, String reason);
}
