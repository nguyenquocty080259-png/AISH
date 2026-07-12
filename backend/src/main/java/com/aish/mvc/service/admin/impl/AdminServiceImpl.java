package com.aish.mvc.service.admin.impl;

import com.aish.mvc.dto.auth.admin.AdminCreateUserRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminResetPasswordRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminUpdateUserRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminUpdateUserStatusRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminUserResponseDTO;
import com.aish.mvc.dto.doc.AdminAppealResponseDTO;
import com.aish.mvc.dto.doc.AdminStatsDTO;
import com.aish.mvc.dto.doc.DocumentSummaryDTO;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthRole;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.ModerationAppeal;
import com.aish.mvc.entity.enums.AppealStatus;
import com.aish.mvc.entity.enums.AuthProviders;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.IngestStatus;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthRoleRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.ModerationAppealRepository;
import com.aish.mvc.repository.doc.SubjectRepository;
import com.aish.mvc.service.admin.AdminService;
import com.aish.mvc.tools.seed.SeedCredentialRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final ModerationAppealRepository moderationAppealRepository;
    private final DocDocumentRepository docDocumentRepository;
    private final AuthUserRepository authUserRepository;
    private final SubjectRepository subjectRepository;
    private final AuthAccountRepository authAccountRepository;
    private final AuthRoleRepository authRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeedCredentialRegistry seedCredentialRegistry;

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
        long docsIngested = docDocumentRepository.countByIngestStatusAndDeletedAtIsNull(IngestStatus.INGESTED);
        long docsNotIngested = docDocumentRepository.countByIngestStatusAndDeletedAtIsNull(IngestStatus.NOT_INGESTED)
                + docDocumentRepository.countByIngestStatusIsNullAndDeletedAtIsNull();
        long docsUnsupported = docDocumentRepository.countByIngestStatusAndDeletedAtIsNull(IngestStatus.UNSUPPORTED_FORMAT);
        return new AdminStatsDTO(totalUsers, totalDocuments, publicDocuments, privateDocuments, pendingAppeals,
                totalSubjects, docsIngested, docsNotIngested, docsUnsupported);
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
                            .seedPassword(seedCredentialRegistry
                                    .getPassword(account.getIdentifier())
                                    .orElse(null))
                            .build();

                })
                .toList();
    }

    @Override
    @Transactional
    public AdminUserResponseDTO createUser(AdminCreateUserRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (authAccountRepository.existsByIdentifier(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại.");
        }

        String roleName = request.getRole() == null || request.getRole().isBlank()
                ? "USER"
                : request.getRole().trim().toUpperCase(Locale.ROOT);
        AuthRole role = authRoleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Vai trò " + roleName + " không tồn tại."));

        AuthUser user = new AuthUser();
        user.setFullName(request.getFullName().trim());
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        authUserRepository.save(user);

        AuthAccount account = new AuthAccount();
        account.setUser(user);
        account.setProvider(AuthProviders.LOCAL);
        account.setIdentifier(email);
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setIsVerified(true);
        account.setIsPrimary(true);
        authAccountRepository.save(account);

        return AdminUserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(account.getIdentifier())
                .avatarUrl(user.getAvatarUrl())
                .role(role.getRoleName())
                .status(user.getStatus().name())
                .online(false)
                .lastLoginAt(account.getLastLoginAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    @Override
    @Transactional
    public AdminUserResponseDTO updateUser(Long userId, AdminUpdateUserRequestDTO request) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));

        String roleName = request.getRole().trim().toUpperCase(Locale.ROOT);
        AuthRole role = authRoleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Vai trò " + roleName + " không tồn tại."));

        user.setFullName(request.getFullName().trim());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setRole(role);
        authUserRepository.save(user);

        AuthAccount account = authAccountRepository.findAll().stream()
                .filter(candidate -> candidate.getUser().getId().equals(userId))
                .filter(candidate -> Boolean.TRUE.equals(candidate.getIsPrimary()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tài khoản chính của người dùng."));

        return AdminUserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(account.getIdentifier())
                .avatarUrl(user.getAvatarUrl())
                .role(role.getRoleName())
                .status(user.getStatus().name())
                .online(false)
                .lastLoginAt(account.getLastLoginAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    @Override
    @Transactional
    public AdminUserResponseDTO updateUserStatus(
            Long userId, AdminUpdateUserStatusRequestDTO request) {
        UserStatus requestedStatus;
        try {
            requestedStatus = UserStatus.valueOf(request.getStatus().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Trạng thái chỉ được là ACTIVE hoặc BANNED.");
        }
        if (requestedStatus != UserStatus.ACTIVE && requestedStatus != UserStatus.BANNED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Trạng thái chỉ được là ACTIVE hoặc BANNED.");
        }

        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));

        if (requestedStatus == UserStatus.BANNED) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String identifier = authentication != null ? authentication.getName() : null;
            AuthAccount currentAdminAccount = authAccountRepository
                    .findByIdentifierWithUserAndRole(identifier)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED, "Không xác định được admin đang thao tác."));

            if (currentAdminAccount.getUser().getId().equals(userId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Admin không thể tự khóa tài khoản của chính mình.");
            }

            boolean targetIsActiveAdmin = user.getStatus() == UserStatus.ACTIVE
                    && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());
            if (targetIsActiveAdmin
                    && authUserRepository.countByRole_RoleNameAndStatus(
                            "ADMIN", UserStatus.ACTIVE) <= 1) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Không thể khóa admin ACTIVE cuối cùng trong hệ thống.");
            }
        }

        user.setStatus(requestedStatus);
        authUserRepository.save(user);

        AuthAccount account = authAccountRepository.findAll().stream()
                .filter(candidate -> candidate.getUser().getId().equals(userId))
                .filter(candidate -> Boolean.TRUE.equals(candidate.getIsPrimary()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tài khoản chính của người dùng."));

        return AdminUserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(account.getIdentifier())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().getRoleName())
                .status(user.getStatus().name())
                .online(false)
                .lastLoginAt(account.getLastLoginAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    @Override
    @Transactional
    public void resetUserPassword(Long userId, AdminResetPasswordRequestDTO request) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));
        AuthAccount account = authAccountRepository.findByUserAndProvider(user, AuthProviders.LOCAL)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Người dùng không có tài khoản đăng nhập LOCAL."));

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        authAccountRepository.save(account);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminIdentifier = authentication != null ? authentication.getName() : "unknown";
        log.info("Admin {} reset password for user id={} identifier={}",
                adminIdentifier, userId, account.getIdentifier());
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
