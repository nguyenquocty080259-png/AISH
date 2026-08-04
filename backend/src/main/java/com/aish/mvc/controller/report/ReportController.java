package com.aish.mvc.controller.report;

import com.aish.mvc.dto.report.CreateReportRequestDTO;
import com.aish.mvc.dto.report.ReportResponseDTO;
import com.aish.mvc.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CỬA NGÕ API cho người dùng THƯỜNG gửi báo cáo vi phạm (tài liệu/bình luận/...) và xem lại report
 * mình đã gửi. Xử lý report (duyệt/từ chối) là việc của Admin, nằm ở AdminController.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // POST /api/reports — gửi báo cáo vi phạm mới.
    @PostMapping
    public ResponseEntity<ReportResponseDTO> createReport(@RequestBody CreateReportRequestDTO request) {
        ReportResponseDTO response = reportService.createReport(
                request.getTargetType(), request.getTargetId(), request.getReason());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/reports/mine — danh sách report tôi đã gửi.
    @GetMapping("/mine")
    public ResponseEntity<List<ReportResponseDTO>> getMyReports() {
        return ResponseEntity.ok(reportService.getMyReports());
    }
}
