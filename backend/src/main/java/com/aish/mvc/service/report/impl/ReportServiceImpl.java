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

/**
 * Cài đặt thật của {@link ReportService}. Điểm quan trọng nhất là {@link #resolveReport}: khi
 * Admin xử lý report, tùy hành động chọn (actionTaken) mà hệ thống có thể TỰ ĐỘNG xoá nội dung vi
 * phạm (removeContent) hoặc khoá tài khoản người vi phạm (lockTargetAccount) — không chỉ đơn
 * thuần ghi nhận trạng thái.
 */
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

    // Lấy user đang đăng nhập từ token.
    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    // Người dùng gửi báo cáo mới. Các bước: (1) kiểm tra targetType hợp lệ; (2) kiểm tra đối
    // tượng bị báo cáo có thật tồn tại; (3) lưu report với trạng thái PENDING; (4) báo cho mọi
    // Admin đang hoạt động biết có report mới cần xử lý.
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

        Report savedReport = reportRepository.save(report); // lưu bảng reports
        authUserRepository.findByRole_RoleNameAndStatus("ADMIN", UserStatus.ACTIVE)
                .forEach(admin -> notificationService.createNotification(
                        admin.getId(),
                        NotificationType.REPORT_CREATED,
                        "Có báo cáo mới cần xử lý (#" + savedReport.getId() + ")",
                        savedReport.getId()));
        return toResponseDTO(savedReport);
    }

    // Hệ thống tự tạo report (không phải người dùng bấm nút) — nguồn SYSTEM, không có reporterUserId.
    @Override
    @Transactional
    public void createSystemReport(ReportTargetType targetType, Long targetId, String reason) {
        Report report = Report.builder()
                .reporterUserId(null)
                .source(ReportSource.SYSTEM)
                .targetType(targetType)
                .targetId(targetId)
                .reason(reason)
                .status(ReportStatus.PENDING)
                .build();
        reportRepository.save(report); // lưu bảng reports
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

    // (Admin) Xử lý report. Đầu vào: id report, hành động chọn (vd REMOVE_CONTENT/LOCK_ACCOUNT/
    // WARN_USER/DISMISSED), phản hồi cho người báo cáo. Các bước: (1) report phải đang PENDING,
    // xử lý 2 lần thì báo lỗi; (2) hành động phải hợp lệ với loại đối tượng (vd không xoá nội
    // dung của USER report); (3) THỰC THI hành động (xoá nội dung / khoá tài khoản — có tác dụng
    // phụ thật, không chỉ đổi trạng thái); (4) ghi lại trạng thái RESOLVED/DISMISSED + người
    // quyết định + thời điểm; (5) báo cho người đã gửi report biết kết quả (report hệ thống thì
    // không có ai để báo).
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

        Report savedReport = reportRepository.save(report); // lưu bảng reports
        if (savedReport.getReporterUserId() != null) {
            notificationService.createNotification(
                    savedReport.getReporterUserId(),
                    NotificationType.REPORT_RESOLVED,
                    "Báo cáo của bạn đã được xử lý: " + savedReport.getActionTaken(),
                    savedReport.getId());
        }
        return toAdminResponseDTO(savedReport);
    }

    // Chỉ report đang chờ (PENDING) mới xử lý được — tránh xử lý trùng lặp một report đã xong.
    private Report requirePendingReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report không tồn tại."));
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("error.report.alreadyProcessed");
        }
        return report;
    }

    // Đọc chuỗi hành động từ request thành enum ReportAction; rỗng/không hợp lệ -> báo lỗi rõ ràng.
    private ReportAction parseActionTaken(String actionTakenRaw) {
        if (actionTakenRaw == null || actionTakenRaw.isBlank()) {
            throw new IllegalArgumentException("error.report.actionRequired");
        }
        try {
            return ReportAction.valueOf(actionTakenRaw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("actionTaken không hợp lệ: " + actionTakenRaw + ".");
        }
    }

    // Chặn hành động không hợp lý với loại đối tượng: DOCUMENT/COMMENT được xoá nội dung, nhưng
    // USER/AI_MESSAGE (không có "nội dung" độc lập để xoá) chỉ cho khoá tài khoản/cảnh báo/bỏ qua.
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

    // Thực thi tác dụng phụ thật của hành động đã chọn. WARN_USER/DISMISSED không có tác dụng
    // phụ nào ngoài việc ghi nhận quyết định.
    private void executeAction(Report report, ReportAction actionTaken) {
        switch (actionTaken) {
            case REMOVE_CONTENT -> removeContent(report);
            case LOCK_ACCOUNT -> lockTargetAccount(report);
            case WARN_USER, DISMISSED -> {
                // No side-effect beyond recording the report decision.
            }
        }
    }

    // Xoá thật nội dung vi phạm: tài liệu -> xoá qua DocumentService (xoá mềm), bình luận -> xoá
    // qua EngagementService.
    private void removeContent(Report report) {
        switch (report.getTargetType()) {
            case DOCUMENT -> documentService.adminDeleteDocument(report.getTargetId());
            case COMMENT -> engagementService.deleteComment(report.getTargetId());
            case USER, AI_MESSAGE -> throw new IllegalArgumentException(
                    "REMOVE_CONTENT không hợp lệ cho targetType " + report.getTargetType() + ".");
        }
    }

    // Khoá tài khoản người liên quan tới report — đổi trạng thái người dùng sang BANNED
    // (dùng lại đúng luồng AdminService.updateUserStatus).
    private void lockTargetAccount(Report report) {
        Long userId = resolveTargetOwnerUserId(report);
        AdminUpdateUserStatusRequestDTO request = new AdminUpdateUserStatusRequestDTO();
        request.setStatus("BANNED");
        adminService.updateUserStatus(userId, request); // đổi trạng thái: -> BANNED
    }

    // Tìm ra "chủ nhân" thật sự của đối tượng bị báo cáo — người sẽ bị khoá tài khoản nếu chọn
    // LOCK_ACCOUNT. DOCUMENT/COMMENT/AI_MESSAGE đều quy về chủ sở hữu; USER thì chính targetId đó.
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

    // Đọc chuỗi loại đối tượng từ request. AI_MESSAGE bị chặn ở đây vì report AI_MESSAGE chỉ tạo
    // được từ hệ thống (createSystemReport), không cho người dùng tự chọn loại này.
    private ReportTargetType parseTargetType(String targetType) {
        if (targetType == null || targetType.isBlank()) {
            throw new IllegalArgumentException("error.report.targetTypeRequired");
        }

        final ReportTargetType parsedTargetType;
        try {
            parsedTargetType = ReportTargetType.valueOf(targetType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("targetType không hợp lệ: " + targetType + ".");
        }

        if (parsedTargetType == ReportTargetType.AI_MESSAGE) {
            throw new IllegalArgumentException("error.report.aiMessageForbidden");
        }
        return parsedTargetType;
    }

    // Kiểm tra đối tượng bị báo cáo có thật tồn tại — tránh report "ma" trỏ tới id không có thật.
    private void validateTargetExists(ReportTargetType targetType, Long targetId) {
        if (targetId == null) {
            throw new IllegalArgumentException("error.report.targetIdRequired");
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

    // DTO rút gọn cho người dùng thường xem report của chính mình.
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

    // DTO đầy đủ cho Admin: kèm email người báo cáo, ai đã xử lý, và nội dung tin nhắn bị gắn cờ (nếu là AI_MESSAGE).
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
                .flaggedMessageContent(findFlaggedMessageContent(report))
                .build();
    }

    // Lấy nội dung tin nhắn AI bị gắn cờ để Admin xem trực tiếp mà không cần mở riêng cuộc trò chuyện.
    private String findFlaggedMessageContent(Report report) {
        if (report.getTargetType() != ReportTargetType.AI_MESSAGE) {
            return null;
        }
        return aiMessageRepository.findById(report.getTargetId())
                .map(AiMessage::getContent)
                .orElse(null);
    }

    // Email người báo cáo (report hệ thống thì không có ai để tra -> trả null).
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
