package com.aish.mvc.service.report.impl;

import com.aish.mvc.dto.auth.admin.AdminUpdateUserStatusRequestDTO;
import com.aish.mvc.dto.report.AdminReportResponseDTO;
import com.aish.mvc.dto.report.ReportResponseDTO;
import com.aish.mvc.entity.ai.AiMessage;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.Comment;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.ReportAction;
import com.aish.mvc.entity.enums.ReportSource;
import com.aish.mvc.entity.enums.ReportStatus;
import com.aish.mvc.entity.enums.ReportTargetType;
import com.aish.mvc.entity.enums.NotificationType;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.entity.report.Report;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.ai.AiMessageRepository;
import com.aish.mvc.repository.doc.CommentRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.report.ReportRepository;
import com.aish.mvc.service.admin.AdminService;
import com.aish.mvc.service.doc.DocumentService;
import com.aish.mvc.service.doc.EngagementService;
import com.aish.mvc.service.notification.NotificationService;
import com.aish.mvc.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final DocDocumentRepository docDocumentRepository;
    private final AuthUserRepository authUserRepository;
    private final CommentRepository commentRepository;
    private final AuthAccountRepository authAccountRepository;
    private final AiMessageRepository aiMessageRepository;
    private final DocumentService documentService;
    private final EngagementService engagementService;
    private final AdminService adminService;
    private final NotificationService notificationService;

    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    @Override
    @Transactional
    public ReportResponseDTO createReport(String targetType, Long targetId, String reason) {
        ReportTargetType parsedTargetType = parseTargetType(targetType);
        validateTargetExists(parsedTargetType, targetId);

        AuthUser currentUser = getCurrentUser();
        Report report = Report.builder()
                .reporterUserId(currentUser.getId())
                .source(ReportSource.USER)
                .targetType(parsedTargetType)
                .targetId(targetId)
                .reason(reason)
                .status(ReportStatus.PENDING)
                .build();

        Report savedReport = reportRepository.save(report);
        authUserRepository.findByRole_RoleNameAndStatus("ADMIN", UserStatus.ACTIVE)
                .forEach(admin -> notificationService.createNotification(
                        admin.getId(),
                        NotificationType.REPORT_CREATED,
                        "Có báo cáo mới cần xử lý (#" + savedReport.getId() + ")",
                        savedReport.getId()));
        return toResponseDTO(savedReport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponseDTO> getMyReports() {
        Long userId = getCurrentUser().getId();
        return reportRepository.findByReporterUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminReportResponseDTO> listReports(ReportStatus status) {
        List<Report> reports = status != null
                ? reportRepository.findByStatusOrderByCreatedAtAsc(status)
                : reportRepository.findAll();
        return reports.stream().map(this::toAdminResponseDTO).toList();
    }

    @Override
    @Transactional
    public AdminReportResponseDTO resolveReport(Long reportId, String actionTakenRaw, String adminResponse) {
        Report report = requirePendingReport(reportId);
        ReportAction actionTaken = parseActionTaken(actionTakenRaw);
        validateActionForTarget(report.getTargetType(), actionTaken);

        executeAction(report, actionTaken);

        report.setStatus(actionTaken == ReportAction.DISMISSED
                ? ReportStatus.DISMISSED
                : ReportStatus.RESOLVED);
        report.setActionTaken(actionTaken);
        report.setAdminResponse(adminResponse);
        report.setDecidedByAdminId(getCurrentUser().getId());
        report.setDecidedAt(LocalDateTime.now());

        Report savedReport = reportRepository.save(report);
        if (savedReport.getReporterUserId() != null) {
            notificationService.createNotification(
                    savedReport.getReporterUserId(),
                    NotificationType.REPORT_RESOLVED,
                    "Báo cáo của bạn đã được xử lý: " + savedReport.getActionTaken(),
                    savedReport.getId());
        }
        return toAdminResponseDTO(savedReport);
    }

    private Report requirePendingReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report không tồn tại."));
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("Report này đã được xử lý; chỉ report PENDING mới có thể xử lý.");
        }
        return report;
    }

    private ReportAction parseActionTaken(String actionTakenRaw) {
        if (actionTakenRaw == null || actionTakenRaw.isBlank()) {
            throw new IllegalArgumentException("actionTaken là bắt buộc.");
        }
        try {
            return ReportAction.valueOf(actionTakenRaw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("actionTaken không hợp lệ: " + actionTakenRaw + ".");
        }
    }

    private void validateActionForTarget(ReportTargetType targetType, ReportAction actionTaken) {
        EnumSet<ReportAction> allowedActions = switch (targetType) {
            case DOCUMENT, COMMENT -> EnumSet.allOf(ReportAction.class);
            case USER, AI_MESSAGE -> EnumSet.of(
                    ReportAction.LOCK_ACCOUNT, ReportAction.WARN_USER, ReportAction.DISMISSED);
        };
        if (!allowedActions.contains(actionTaken)) {
            throw new IllegalArgumentException(
                    "Action " + actionTaken + " không hợp lệ cho targetType " + targetType + ".");
        }
    }

    private void executeAction(Report report, ReportAction actionTaken) {
        switch (actionTaken) {
            case REMOVE_CONTENT -> removeContent(report);
            case LOCK_ACCOUNT -> lockTargetAccount(report);
            case WARN_USER, DISMISSED -> {
                // No side-effect beyond recording the report decision.
            }
        }
    }

    private void removeContent(Report report) {
        switch (report.getTargetType()) {
            case DOCUMENT -> documentService.adminDeleteDocument(report.getTargetId());
            case COMMENT -> engagementService.deleteComment(report.getTargetId());
            case USER, AI_MESSAGE -> throw new IllegalArgumentException(
                    "REMOVE_CONTENT không hợp lệ cho targetType " + report.getTargetType() + ".");
        }
    }

    private void lockTargetAccount(Report report) {
        Long userId = resolveTargetOwnerUserId(report);
        AdminUpdateUserStatusRequestDTO request = new AdminUpdateUserStatusRequestDTO();
        request.setStatus("BANNED");
        adminService.updateUserStatus(userId, request);
    }

    private Long resolveTargetOwnerUserId(Report report) {
        return switch (report.getTargetType()) {
            case DOCUMENT -> {
                DocDocument document = docDocumentRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại."));
                yield document.getUser().getId();
            }
            case COMMENT -> {
                Comment comment = commentRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new ResourceNotFoundException("Bình luận không tồn tại."));
                yield comment.getUser().getId();
            }
            case USER -> report.getTargetId();
            case AI_MESSAGE -> {
                AiMessage message = aiMessageRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new ResourceNotFoundException("AI message không tồn tại."));
                yield message.getConversation().getUser().getId();
            }
        };
    }

    private ReportTargetType parseTargetType(String targetType) {
        if (targetType == null || targetType.isBlank()) {
            throw new IllegalArgumentException("targetType không được để trống.");
        }

        final ReportTargetType parsedTargetType;
        try {
            parsedTargetType = ReportTargetType.valueOf(targetType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("targetType không hợp lệ: " + targetType + ".");
        }

        if (parsedTargetType == ReportTargetType.AI_MESSAGE) {
            throw new IllegalArgumentException("User không được report AI_MESSAGE; loại report này chỉ do hệ thống tạo.");
        }
        return parsedTargetType;
    }

    private void validateTargetExists(ReportTargetType targetType, Long targetId) {
        if (targetId == null) {
            throw new IllegalArgumentException("targetId không được để trống.");
        }

        boolean exists = switch (targetType) {
            case DOCUMENT -> docDocumentRepository.existsById(targetId);
            case USER -> authUserRepository.existsById(targetId);
            case COMMENT -> commentRepository.existsById(targetId);
            case AI_MESSAGE -> false;
        };

        if (!exists) {
            throw new IllegalArgumentException("Không tìm thấy " + targetType + " với id " + targetId + ".");
        }
    }

    private ReportResponseDTO toResponseDTO(Report report) {
        return ReportResponseDTO.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .status(report.getStatus())
                .actionTaken(report.getActionTaken())
                .adminResponse(report.getAdminResponse())
                .createdAt(report.getCreatedAt())
                .decidedAt(report.getDecidedAt())
                .build();
    }

    private AdminReportResponseDTO toAdminResponseDTO(Report report) {
        return AdminReportResponseDTO.builder()
                .id(report.getId())
                .reporterUserId(report.getReporterUserId())
                .reporterEmail(findReporterEmail(report))
                .source(report.getSource())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .status(report.getStatus())
                .actionTaken(report.getActionTaken())
                .adminResponse(report.getAdminResponse())
                .createdAt(report.getCreatedAt())
                .decidedAt(report.getDecidedAt())
                .decidedByAdminId(report.getDecidedByAdminId())
                .build();
    }

    private String findReporterEmail(Report report) {
        if (report.getSource() == ReportSource.SYSTEM || report.getReporterUserId() == null) {
            return null;
        }
        return authAccountRepository.findAll().stream()
                .filter(account -> account.getUser().getId().equals(report.getReporterUserId()))
                .filter(account -> Boolean.TRUE.equals(account.getIsPrimary()))
                .map(account -> account.getIdentifier())
                .findFirst()
                .orElse(null);
    }
}
