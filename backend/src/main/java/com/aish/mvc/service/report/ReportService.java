package com.aish.mvc.service.report;

import com.aish.mvc.dto.report.AdminReportResponseDTO;
import com.aish.mvc.dto.report.ReportResponseDTO;
import com.aish.mvc.entity.enums.ReportStatus;

import java.util.List;

public interface ReportService {

    ReportResponseDTO createReport(String targetType, Long targetId, String reason);

    List<ReportResponseDTO> getMyReports();

    List<AdminReportResponseDTO> listReports(ReportStatus status);

    AdminReportResponseDTO resolveReport(Long reportId, String actionTakenRaw, String adminResponse);
}
