package com.aish.mvc.service.report;

import com.aish.mvc.dto.report.ReportResponseDTO;

import java.util.List;

public interface ReportService {

    ReportResponseDTO createReport(String targetType, Long targetId, String reason);

    List<ReportResponseDTO> getMyReports();
}
