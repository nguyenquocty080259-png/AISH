package com.aish.mvc.service.interaction.impl;

import com.aish.mvc.dto.interaction.CaseMessageDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.ModerationAppeal;
import com.aish.mvc.entity.enums.CaseType;
import com.aish.mvc.entity.enums.NotificationType;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.entity.interaction.CaseMessage;
import com.aish.mvc.entity.report.Report;
import com.aish.mvc.exception.ForbiddenException;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.ModerationAppealRepository;
import com.aish.mvc.repository.interaction.CaseMessageRepository;
import com.aish.mvc.repository.report.ReportRepository;
import com.aish.mvc.service.interaction.CaseMessageService;
import com.aish.mvc.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaseMessageServiceImpl implements CaseMessageService {

    private static final int MAX_CONTENT_LENGTH = 2000;
    private static final Logger log = LoggerFactory.getLogger(CaseMessageServiceImpl.class);

    private final CaseMessageRepository caseMessageRepository;
    private final ReportRepository reportRepository;
    private final ModerationAppealRepository moderationAppealRepository;
    private final AuthAccountRepository authAccountRepository;
    private final AuthUserRepository authUserRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<CaseMessageDTO> listMessages(CaseType caseType, Long caseId) {
        assertCanAccess(caseType, caseId);
        return caseMessageRepository.findByCaseTypeAndCaseIdOrderByCreatedAtAsc(caseType, caseId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CaseMessageDTO postMessage(CaseType caseType, Long caseId, String content) {
        assertCanAccess(caseType, caseId);
        String cleaned = validateContent(content);

        AuthUser currentUser = getCurrentUser();
        CaseMessage message = CaseMessage.builder()
                .caseType(caseType)
                .caseId(caseId)
                .senderUserId(currentUser.getId())
                .senderRole(currentUser.getRole().getRoleName())
                .content(cleaned)
                .build();
        CaseMessage saved = caseMessageRepository.save(message);
        notifyCaseReply(caseType, caseId, currentUser);
        return toDTO(saved);
    }

    private void notifyCaseReply(CaseType caseType, Long caseId, AuthUser currentUser) {
        try {
            boolean isSenderAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole().getRoleName());
            String caseLabel = caseType == CaseType.REPORT ? "báo cáo" : "kháng cáo";

            if (isSenderAdmin) {
                Long ownerId = resolveCaseOwnerId(caseType, caseId);
                if (ownerId != null && !ownerId.equals(currentUser.getId())) {
                    notificationService.createCaseNotification(
                            ownerId,
                            NotificationType.CASE_REPLY,
                            "Có phản hồi mới cho " + caseLabel + " của bạn.",
                            caseType,
                            caseId);
                }
            } else {
                authUserRepository.findByRole_RoleNameAndStatus("ADMIN", UserStatus.ACTIVE).stream()
                        .filter(admin -> !admin.getId().equals(currentUser.getId()))
                        .forEach(admin -> notificationService.createCaseNotification(
                                admin.getId(),
                                NotificationType.CASE_REPLY,
                                "Có phản hồi mới từ người dùng trên " + caseLabel + " #" + caseId + ".",
                                caseType,
                                caseId));
            }
        } catch (Exception ex) {
            log.error("Failed to send CASE_REPLY notification for caseType={}, caseId={}", caseType, caseId, ex);
        }
    }

    private String validateContent(String content) {
        String cleaned = content == null ? "" : content.trim();
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("error.caseMessage.empty");
        }
        if (cleaned.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("error.caseMessage.tooLong");
        }
        return cleaned;
    }

    private void assertCanAccess(CaseType caseType, Long caseId) {
        AuthUser currentUser = getCurrentUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole().getRoleName());
        Long ownerId = resolveCaseOwnerId(caseType, caseId);

        if (!isAdmin && (ownerId == null || !ownerId.equals(currentUser.getId()))) {
            throw new ForbiddenException("error.caseMessage.forbidden");
        }
    }

    private Long resolveCaseOwnerId(CaseType caseType, Long caseId) {
        if (caseType == CaseType.REPORT) {
            Report report = reportRepository.findById(caseId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.caseMessage.reportNotFound"));
            return report.getReporterUserId();
        }

        ModerationAppeal appeal = moderationAppealRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("error.caseMessage.appealNotFound"));
        return appeal.getUser().getId();
    }

    private CaseMessageDTO toDTO(CaseMessage message) {
        String senderName = authUserRepository.findById(message.getSenderUserId())
                .map(AuthUser::getFullName)
                .orElse(null);
        return new CaseMessageDTO(
                message.getId(),
                message.getSenderUserId(),
                senderName,
                message.getSenderRole(),
                message.getContent(),
                message.getCreatedAt());
    }

    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }
}
