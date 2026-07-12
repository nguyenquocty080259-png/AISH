package com.aish.mvc.dto.report;

import com.aish.mvc.entity.enums.ReportAction;
import com.aish.mvc.entity.enums.ReportSource;
import com.aish.mvc.entity.enums.ReportStatus;
import com.aish.mvc.entity.enums.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminReportResponseDTO {

    private Long id;
    private Long reporterUserId;
    private String reporterEmail;
    private ReportSource source;
    private ReportTargetType targetType;
    private Long targetId;
    private String reason;
    private ReportStatus status;
    private ReportAction actionTaken;
    private String adminResponse;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
    private Long decidedByAdminId;
}
