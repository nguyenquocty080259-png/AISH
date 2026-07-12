package com.aish.mvc.service.report.impl;

import com.aish.mvc.dto.report.ReportResponseDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.ReportSource;
import com.aish.mvc.entity.enums.ReportStatus;
import com.aish.mvc.entity.enums.ReportTargetType;
import com.aish.mvc.entity.report.Report;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.CommentRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.report.ReportRepository;
import com.aish.mvc.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return toResponseDTO(reportRepository.save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponseDTO> getMyReports() {
        Long userId = getCurrentUser().getId();
        return reportRepository.findByReporterUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponseDTO)
                .toList();
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
}
