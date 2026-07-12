package com.aish.mvc.repository.report;

import com.aish.mvc.entity.enums.ReportStatus;
import com.aish.mvc.entity.report.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByStatusOrderByCreatedAtAsc(ReportStatus status);

    List<Report> findByReporterUserIdOrderByCreatedAtDesc(Long reporterUserId);
}
