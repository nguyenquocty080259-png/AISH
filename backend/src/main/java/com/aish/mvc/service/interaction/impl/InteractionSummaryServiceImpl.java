package com.aish.mvc.service.interaction.impl;

import com.aish.mvc.dto.interaction.InteractionSummaryDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.AppealStatus;
import com.aish.mvc.entity.enums.ReportStatus;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.ModerationAppealRepository;
import com.aish.mvc.repository.notification.NotificationRepository;
import com.aish.mvc.repository.report.ReportRepository;
import com.aish.mvc.service.interaction.InteractionSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InteractionSummaryServiceImpl implements InteractionSummaryService {

    private final NotificationRepository notificationRepository;
    private final ReportRepository reportRepository;
    private final ModerationAppealRepository moderationAppealRepository;
    private final AuthAccountRepository authAccountRepository;

    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    @Override
    @Transactional(readOnly = true)
    public InteractionSummaryDTO getSummary() {
        AuthUser currentUser = getCurrentUser();
        Long userId = currentUser.getId();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole().getRoleName());

        return InteractionSummaryDTO.builder()
                .unreadNotifications(notificationRepository.countByRecipientUserIdAndIsReadFalse(userId))
                .myPendingReports(reportRepository.countByReporterUserIdAndStatus(userId, ReportStatus.PENDING))
                .myPendingAppeals(moderationAppealRepository.countByUser_IdAndStatus(userId, AppealStatus.APPEAL_PENDING))
                .adminPendingReports(isAdmin ? reportRepository.countByStatus(ReportStatus.PENDING) : 0L)
                .adminPendingAppeals(isAdmin ? moderationAppealRepository.countByStatus(AppealStatus.APPEAL_PENDING) : 0L)
                .build();
    }
}
