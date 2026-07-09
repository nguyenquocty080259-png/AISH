package com.aish.mvc.service.admin.impl;

import com.aish.mvc.dto.auth.admin.AdminUserResponseDTO;
import com.aish.mvc.dto.doc.AdminAppealResponseDTO;
import com.aish.mvc.dto.doc.AdminStatsDTO;
import com.aish.mvc.dto.doc.DocumentSummaryDTO;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.ModerationAppeal;
import com.aish.mvc.entity.enums.AppealStatus;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.ModerationAppealRepository;
import com.aish.mvc.repository.doc.SubjectRepository;
import com.aish.mvc.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ModerationAppealRepository moderationAppealRepository;
    private final DocDocumentRepository docDocumentRepository;
    private final AuthUserRepository authUserRepository;
    private final SubjectRepository subjectRepository;
    private final AuthAccountRepository authAccountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdminAppealResponseDTO> listAppeals(AppealStatus status) {
        List<ModerationAppeal> appeals = status != null
                ? moderationAppealRepository.findByStatusOrderByCreatedAtAsc(status)
                : moderationAppealRepository.findAll();
        return appeals.stream().map(this::toAdminDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminAppealResponseDTO approveAppeal(Long appealId, String adminNote) {
        ModerationAppeal appeal = requirePendingAppeal(appealId);

        appeal.setStatus(AppealStatus.APPEAL_APPROVED);
        appeal.setAdminNote(adminNote);

        // DEC-009: admin gỡ/chuyển trạng thái nhưng KHÔNG trở thành owner — document.user
        // không hề bị đụng tới ở đây.
        DocDocument doc = appeal.getDocument();
        doc.setVisibility(DocumentVisibility.PUBLIC);
        doc.setModerationStatus(ModerationStatus.APPROVED);
        docDocumentRepository.save(doc);

        moderationAppealRepository.save(appeal);
        return toAdminDTO(appeal);
    }

    @Override
    @Transactional
    public AdminAppealResponseDTO rejectAppeal(Long appealId, String adminNote) {
        ModerationAppeal appeal = requirePendingAppeal(appealId);

        appeal.setStatus(AppealStatus.APPEAL_REJECTED);
        appeal.setAdminNote(adminNote);
        moderationAppealRepository.save(appeal);
        return toAdminDTO(appeal);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatsDTO getStats() {
        long totalUsers = authUserRepository.count();
        long totalDocuments = docDocumentRepository.countByDeletedAtIsNull();
        long publicDocuments = docDocumentRepository.countByVisibilityAndDeletedAtIsNull(DocumentVisibility.PUBLIC);
        long privateDocuments = docDocumentRepository.countByVisibilityAndDeletedAtIsNull(DocumentVisibility.PRIVATE);
        long pendingAppeals = moderationAppealRepository.countByStatus(AppealStatus.APPEAL_PENDING);
        long totalSubjects = subjectRepository.count();
        return new AdminStatsDTO(totalUsers, totalDocuments, publicDocuments, privateDocuments, pendingAppeals, totalSubjects);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponseDTO> getAllUsers() {

        List<AuthAccount> accounts = authAccountRepository.findAll();

        return accounts.stream()
                .map(account -> {

                    AuthUser user = account.getUser();

                    return AdminUserResponseDTO.builder()
                            .id(user.getId())
                            .fullName(user.getFullName())
                            .email(account.getIdentifier())
                            .avatarUrl(user.getAvatarUrl())
                            .role(user.getRole().getRoleName())
                            .status(user.getStatus().name())
                            .lastLoginAt(account.getLastLoginAt())
                            .online(false) // sẽ xử lý sau
                            .build();

                })
                .toList();
    }

    private ModerationAppeal requirePendingAppeal(Long appealId) {
        ModerationAppeal appeal = moderationAppealRepository.findById(appealId)
                .orElseThrow(() -> new ResourceNotFoundException("Kháng cáo không tồn tại!"));
        if (appeal.getStatus() != AppealStatus.APPEAL_PENDING) {
            throw new IllegalStateException("Kháng cáo này đã được xử lý rồi.");
        }
        return appeal;
    }

    private AdminAppealResponseDTO toAdminDTO(ModerationAppeal appeal) {
        DocDocument doc = appeal.getDocument();
        DocumentSummaryDTO documentSummary = new DocumentSummaryDTO(
                doc.getId(),
                doc.getTitle(),
                doc.getUser() != null ? doc.getUser().getFullName() : null,
                doc.getModerationReason());

        return new AdminAppealResponseDTO(
                appeal.getId(),
                documentSummary,
                appeal.getUser() != null ? appeal.getUser().getFullName() : null,
                appeal.getReason(),
                appeal.getCreatedAt(),
                appeal.getStatus());
    }
}
