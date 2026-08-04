package com.aish.mvc.service.admin.impl;

import com.aish.mvc.dto.auth.admin.AdminCreateUserRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminResetPasswordRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminUpdateUserRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminUpdateUserStatusRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminUserResponseDTO;
import com.aish.mvc.dto.doc.AdminAppealResponseDTO;
import com.aish.mvc.dto.doc.AdminStatsDTO;
import com.aish.mvc.dto.doc.AdminCommentReviewDTO;
import com.aish.mvc.dto.doc.DocumentSummaryDTO;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthRole;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.ModerationAppeal;
import com.aish.mvc.entity.doc.Comment;
import com.aish.mvc.entity.enums.AppealStatus;
import com.aish.mvc.entity.enums.AuthProviders;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.IngestStatus;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.entity.enums.CommentStatus;
import com.aish.mvc.entity.enums.NotificationType;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthRoleRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.DocFileRepository;
import com.aish.mvc.repository.doc.ModerationAppealRepository;
import com.aish.mvc.repository.doc.CommentRepository;
import com.aish.mvc.repository.doc.SubjectRepository;
import com.aish.mvc.service.admin.AdminService;
import com.aish.mvc.service.notification.NotificationService;
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
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/** Cài đặt thật của {@link AdminService}. Xem chi tiết nghiệp vụ ở comment trên từng method của interface. */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final ModerationAppealRepository moderationAppealRepository;
    private final CommentRepository commentRepository;
    private final DocDocumentRepository docDocumentRepository;
    private final DocFileRepository docFileRepository;
    private final AuthUserRepository authUserRepository;
    private final SubjectRepository subjectRepository;
    private final AuthAccountRepository authAccountRepository;
    private final AuthRoleRepository authRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeedCredentialRegistry seedCredentialRegistry;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<AdminAppealResponseDTO> listAppeals(AppealStatus status) {
        List<ModerationAppeal> appeals = status != null
                ? moderationAppealRepository.findByStatusOrderByCreatedAtAsc(status)
                : moderationAppealRepository.findAll();
        return appeals.stream().map(this::toAdminDTO).collect(Collectors.toList());
    }

    // Chấp nhận kháng cáo: đổi appeal sang APPEAL_APPROVED, và tài liệu được chuyển thẳng sang
    // PUBLIC + APPROVED (không cần AI kiểm duyệt lại). Ghi lại admin nào duyệt + thời điểm.
    @Override
    @Transactional
    public AdminAppealResponseDTO approveAppeal(Long appealId, String adminNote) {
        ModerationAppeal appeal = requirePendingAppeal(appealId);

        appeal.setStatus(AppealStatus.APPEAL_APPROVED);
        appeal.setAdminNote(adminNote);

        // DEC-009: admin gỡ/chuyển trạng thái nhưng KHÔNG trở thành owner — document.user
        // không hề bị đụng tới ở đây.
        DocDocument doc = appeal.getDocument();
        doc.setVisibility(DocumentVisibility.PUBLIC); // đổi trạng thái: visibility -> PUBLIC
        doc.setModerationStatus(ModerationStatus.APPROVED); // đổi trạng thái: moderationStatus -> APPROVED
        doc.setAdminReviewedAt(LocalDateTime.now());
        doc.setAdminReviewedBy(getCurrentAdminId());
        docDocumentRepository.save(doc); // lưu bảng doc_documents

        moderationAppealRepository.save(appeal); // lưu bảng moderation_appeals
        notifyAppealDecision(appeal, NotificationType.APPEAL_APPROVED);
        return toAdminDTO(appeal);
    }

    // Từ chối kháng cáo: chỉ đổi trạng thái appeal, tài liệu GIỮ NGUYÊN PRIVATE/REJECTED.
    @Override
    @Transactional
    public AdminAppealResponseDTO rejectAppeal(Long appealId, String adminNote) {
        ModerationAppeal appeal = requirePendingAppeal(appealId);

        appeal.setStatus(AppealStatus.APPEAL_REJECTED);
        appeal.setAdminNote(adminNote);
        moderationAppealRepository.save(appeal); // lưu bảng moderation_appeals
        notifyAppealDecision(appeal, NotificationType.APPEAL_REJECTED);
        return toAdminDTO(appeal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminCommentReviewDTO> listComments(CommentStatus status) {
        List<Comment> comments = status == null
                ? commentRepository.findAllByOrderByCreatedAtDesc()
                : commentRepository.findByStatusOrderByCreatedAtDesc(status);
        return comments.stream().map(this::toAdminCommentDTO).toList();
    }

    @Override
    @Transactional
    public AdminCommentReviewDTO approveComment(Long commentId) {
        return reviewComment(commentId, true);
    }

    @Override
    @Transactional
    public AdminCommentReviewDTO rejectComment(Long commentId) {
        return reviewComment(commentId, false);
    }

    // Admin TỰ TAY xác nhận tài liệu đủ điều kiện public — dùng khi Admin muốn ghi đè/bỏ qua kết
    // quả AI, không qua luồng kháng cáo.
    @Override
    @Transactional
    public void approveDocumentReview(Long documentId) {
        DocDocument document = requireActiveDocument(documentId);
        document.setVisibility(DocumentVisibility.PUBLIC); // đổi trạng thái: visibility -> PUBLIC
        document.setModerationStatus(ModerationStatus.APPROVED); // đổi trạng thái: moderationStatus -> APPROVED
        document.setAdminReviewedAt(LocalDateTime.now());
        document.setAdminReviewedBy(getCurrentAdminId());
        docDocumentRepository.save(document); // lưu bảng doc_documents
        notifyDocumentReviewDecision(document, NotificationType.DOC_APPROVED,
                "Tài liệu \"" + document.getTitle() + "\" của bạn đã được duyệt và công khai.");
    }

    // Admin từ chối tài liệu: chuyển về PRIVATE + REJECTED.
    @Override
    @Transactional
    public void removeDocumentReview(Long documentId) {
        DocDocument document = requireActiveDocument(documentId);
        document.setVisibility(DocumentVisibility.PRIVATE); // đổi trạng thái: visibility -> PRIVATE
        document.setModerationStatus(ModerationStatus.REJECTED); // đổi trạng thái: moderationStatus -> REJECTED
        document.setAdminReviewedAt(LocalDateTime.now());
        document.setAdminReviewedBy(getCurrentAdminId());
        docDocumentRepository.save(document); // lưu bảng doc_documents
        notifyDocumentReviewDecision(document, NotificationType.DOC_REJECTED,
                "Tài liệu \"" + document.getTitle() + "\" của bạn không được duyệt công khai và vẫn ở chế độ riêng tư.");
    }

    // Số liệu tổng quan cho dashboard Admin (tổng user, tài liệu public/private, kháng nghị
    // chờ, tình trạng ingest, dung lượng lưu trữ đã dùng...).
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
        long usedLocalBytes = docFileRepository.sumLocalFileSizeAll();
        long usedCloudBytes = docFileRepository.sumCloudFileSizeAll();
        return new AdminStatsDTO(totalUsers, totalDocuments, publicDocuments, privateDocuments, pendingAppeals,
                totalSubjects, docsIngested, docsNotIngested, docsUnsupported, usedLocalBytes, usedCloudBytes);
    }

    // Toàn bộ người dùng trong hệ thống, cho trang quản lý người dùng của Admin.
    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponseDTO> getAllUsers() {

        List<AuthAccount> accounts = authAccountRepository.findAll();

        return accounts.stream()
                .map(this::toAdminUserResponseDTO)
                .toList();
    }

    // Admin tạo tài khoản mới trực tiếp — bỏ qua hẳn luồng đăng ký + xác minh OTP thông thường
    // (tài khoản được tạo với isVerified=true, ACTIVE ngay).
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
        authUserRepository.save(user); // lưu bảng auth_users

        AuthAccount account = new AuthAccount();
        account.setUser(user);
        account.setProvider(AuthProviders.LOCAL);
        account.setIdentifier(email);
        account.setPasswordHash(passwordEncoder.encode(request.getPassword())); // băm mật khẩu
        account.setIsVerified(true);
        account.setIsPrimary(true);
        authAccountRepository.save(account); // lưu bảng auth_accounts

        return toAdminUserResponseDTO(account);
    }

    // Admin sửa thông tin người dùng (họ tên, avatar, vai trò).
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
        authUserRepository.save(user); // lưu bảng auth_users

        AuthAccount account = authAccountRepository.findAll().stream()
                .filter(candidate -> candidate.getUser().getId().equals(userId))
                .filter(candidate -> Boolean.TRUE.equals(candidate.getIsPrimary()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tài khoản chính của người dùng."));

        return toAdminUserResponseDTO(account);
    }

    // Admin đổi trạng thái tài khoản (chỉ nhận ACTIVE hoặc BANNED). Trước khi khoá (BANNED) có 2
    // chốt an toàn: (1) Admin không được tự khoá chính mình; (2) không được khoá nốt Admin ACTIVE
    // cuối cùng còn lại trong hệ thống (tránh hệ thống mất hết quyền quản trị).
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

        user.setStatus(requestedStatus); // đổi trạng thái: -> ACTIVE hoặc BANNED
        authUserRepository.save(user); // lưu bảng auth_users

        AuthAccount account = authAccountRepository.findAll().stream()
                .filter(candidate -> candidate.getUser().getId().equals(userId))
                .filter(candidate -> Boolean.TRUE.equals(candidate.getIsPrimary()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tài khoản chính của người dùng."));

        return toAdminUserResponseDTO(account);
    }

    // Admin đặt lại mật khẩu cho người dùng (chỉ áp dụng cho tài khoản LOCAL — OAuth không có mật
    // khẩu để đặt lại). Có ghi log ai đã thực hiện, cho ai, để truy vết sau này.
    @Override
    @Transactional
    public void resetUserPassword(Long userId, AdminResetPasswordRequestDTO request) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));
        AuthAccount account = authAccountRepository.findByUserAndProvider(user, AuthProviders.LOCAL)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Người dùng không có tài khoản đăng nhập LOCAL."));

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword())); // băm mật khẩu mới
        authAccountRepository.save(account); // lưu bảng auth_accounts

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminIdentifier = authentication != null ? authentication.getName() : "unknown";
        log.info("Admin {} reset password for user id={} identifier={}",
                adminIdentifier, userId, account.getIdentifier());
    }

    // Dựng DTO người dùng cho Admin xem — kèm cả mật khẩu seed (nếu là tài khoản demo/seed data,
    // để Admin biết mật khẩu test mà không cần tra database).
    private AdminUserResponseDTO toAdminUserResponseDTO(AuthAccount account) {
        AuthUser user = account.getUser();
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
                .seedPassword(seedCredentialRegistry
                        .getPassword(account.getIdentifier())
                        .orElse(null))
                .build();
    }

    // Chỉ kháng cáo đang chờ (APPEAL_PENDING) mới xử lý được — tránh duyệt/từ chối lại kháng cáo đã xong.
    private ModerationAppeal requirePendingAppeal(Long appealId) {
        ModerationAppeal appeal = moderationAppealRepository.findById(appealId)
                .orElseThrow(() -> new ResourceNotFoundException("Kháng cáo không tồn tại!"));
        if (appeal.getStatus() != AppealStatus.APPEAL_PENDING) {
            throw new IllegalStateException("error.admin.appealAlreadyHandled");
        }
        return appeal;
    }

    // Tài liệu phải tồn tại và CHƯA bị xoá mềm mới cho Admin duyệt/từ chối.
    private DocDocument requireActiveDocument(Long documentId) {
        DocDocument document = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));
        if (document.getDeletedAt() != null) {
            throw new ResourceNotFoundException("error.admin.documentDeleted");
        }
        return document;
    }

    // Dùng chung cho cả approveComment và rejectComment. Chỉ bình luận đang PENDING_REVIEW (AI đã
    // gắn cờ, chờ Admin) mới xử lý được. Duyệt -> VISIBLE (hiện lại cho mọi người); từ chối ->
    // REJECTED (giữ ẩn). Sau khi lưu, báo cho chủ bình luận biết kết quả, và nếu được duyệt thì
    // báo thêm cho chủ tài liệu biết có bình luận mới (trừ khi tự bình luận trên tài liệu của mình).
    private AdminCommentReviewDTO reviewComment(Long commentId, boolean approved) {
        Comment comment = commentRepository.findWithUserAndDocumentById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận không tồn tại!"));
        if (comment.getStatus() != CommentStatus.PENDING_REVIEW) {
            throw new IllegalStateException("error.admin.commentNotPending");
        }

        comment.setStatus(approved ? CommentStatus.VISIBLE : CommentStatus.REJECTED); // đổi trạng thái bình luận
        comment.setReviewedBy(getCurrentAdminId());
        comment.setReviewedAt(LocalDateTime.now());
        Comment saved = commentRepository.saveAndFlush(comment); // lưu bảng comments

        try {
            notificationService.notifyCommentReviewed(
                    saved.getUser().getId(), saved.getId(), saved.getDocument().getId(), approved);
        } catch (Exception exception) {
            log.warn("Không thể gửi thông báo kết quả duyệt bình luận {}; quyết định vẫn được lưu.",
                    saved.getId(), exception);
        }

        if (approved && !saved.getUser().getId().equals(saved.getDocument().getUser().getId())) {
            try {
                notificationService.createDocumentNotification(
                        saved.getDocument().getUser().getId(),
                        NotificationType.COMMENT_ON_MY_DOC,
                        "Có bình luận mới trên tài liệu \"" + saved.getDocument().getTitle() + "\" của bạn.",
                        saved.getDocument().getId());
            } catch (Exception exception) {
                log.warn("Không thể gửi thông báo bình luận mới cho chủ tài liệu {}; quyết định vẫn được lưu.",
                        saved.getDocument().getId(), exception);
            }
        }
        return toAdminCommentDTO(saved);
    }

    // Báo cho chủ tài liệu biết kết quả duyệt (approve/reject) — lỗi gửi thông báo không huỷ quyết định đã lưu.
    private void notifyDocumentReviewDecision(DocDocument document, NotificationType type, String message) {
        try {
            notificationService.createDocumentNotification(
                    document.getUser().getId(), type, message, document.getId());
        } catch (Exception exception) {
            log.warn("Không thể gửi thông báo kết quả duyệt tài liệu {} cho chủ sở hữu; quyết định vẫn được lưu.",
                    document.getId(), exception);
        }
    }

    // Báo cho người kháng cáo biết kết quả (chấp nhận/từ chối).
    private void notifyAppealDecision(ModerationAppeal appeal, NotificationType type) {
        try {
            String title = appeal.getDocument().getTitle();
            String message = type == NotificationType.APPEAL_APPROVED
                    ? "Kháng cáo cho tài liệu \"" + title + "\" đã được chấp nhận."
                    : "Kháng cáo cho tài liệu \"" + title + "\" đã bị từ chối.";
            notificationService.createDocumentNotification(
                    appeal.getUser().getId(), type, message, appeal.getDocument().getId());
        } catch (Exception exception) {
            log.warn("Không thể gửi thông báo kết quả kháng cáo {} cho người kháng cáo; quyết định vẫn được lưu.",
                    appeal.getId(), exception);
        }
    }

    // Lấy id của Admin đang đăng nhập (để ghi lại "ai đã duyệt/từ chối").
    private Long getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String identifier = authentication != null ? authentication.getName() : null;
        return authAccountRepository.findByIdentifierWithUserAndRole(identifier)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Không xác định được admin đang thao tác."))
                .getUser()
                .getId();
    }

    // Dựng DTO kháng cáo cho danh sách Admin xem.
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

    // Dựng DTO bình luận cho danh sách Admin duyệt.
    private AdminCommentReviewDTO toAdminCommentDTO(Comment comment) {
        return new AdminCommentReviewDTO(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getFullName(),
                comment.getDocument().getId(),
                comment.getDocument().getTitle(),
                comment.getModerationReason(),
                comment.getDisputeNote(),
                comment.getCreatedAt(),
                comment.getStatus());
    }
}
