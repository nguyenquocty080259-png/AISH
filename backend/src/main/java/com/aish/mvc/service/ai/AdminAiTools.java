package com.aish.mvc.service.ai;

import com.aish.mvc.dto.doc.AdminStatsDTO;
import com.aish.mvc.dto.ai.AiUsageStatsDTO;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * "CÔNG CỤ" mà AI Chat được phép tự gọi (@Tool) khi người đang chat là ADMIN và hỏi về số liệu
 * TOÀN HỆ THỐNG: thống kê tổng quan, chi phí/lượng dùng AI, tìm tài liệu, tìm người dùng, trạng
 * thái chi tiết một tài liệu/một người dùng. Chỉ nên có hiệu lực khi AI đã biết người hỏi là
 * Admin (chốt quyền thật vẫn nằm ở kiểm tra vai trò tại tầng cấu hình AI chat/SecurityConfig).
 */
@Component
@RequiredArgsConstructor
public class AdminAiTools {

    private static final Logger log = LoggerFactory.getLogger(AdminAiTools.class);
    private static final String UNAVAILABLE_MESSAGE = "Không lấy được số liệu hệ thống lúc này.";

    private final AdminService adminService;
    private final DocDocumentRepository docDocumentRepository;
    private final AuthAccountRepository authAccountRepository;
    private final AuthUserRepository authUserRepository;
    private final AiUsageStatsService aiUsageStatsService;

    // AI gọi khi Admin hỏi số liệu tổng quan hệ thống (tổng user, tổng tài liệu, kháng nghị chờ...).
    @Tool(description = "Use this tool when an ADMIN asks for current live, system-wide HiveMind statistics: "
            + "total users, total documents, public/private document counts, pending appeals, total subjects, "
            + "or document ingest status counts. Always use it instead of guessing these numbers.")
    public String getSystemStats() {
        try {
            AdminStatsDTO stats = adminService.getStats();
            return """
                    Tổng người dùng: %d
                    Tổng tài liệu: %d
                    Tài liệu công khai: %d
                    Tài liệu riêng tư: %d
                    Kháng nghị đang chờ: %d
                    Tổng chủ đề: %d
                    Tài liệu đã ingest: %d
                    Tài liệu chưa ingest: %d
                    Tài liệu không hỗ trợ ingest: %d""".formatted(
                    stats.getTotalUsers(),
                    stats.getTotalDocuments(),
                    stats.getPublicDocuments(),
                    stats.getPrivateDocuments(),
                    stats.getPendingAppeals(),
                    stats.getTotalSubjects(),
                    stats.getDocsIngested(),
                    stats.getDocsNotIngested(),
                    stats.getDocsUnsupported());
        }
        catch (Exception exception) {
            log.warn("Không lấy được số liệu hệ thống cho AI admin: {}", exception.getMessage());
            return UNAVAILABLE_MESSAGE;
        }
    }

    // AI gọi khi Admin hỏi về mức dùng AI (số lượt gọi, token, chi phí) hôm nay / 7 ngày qua.
    @Tool(description = "Use this tool when an ADMIN asks about AI/Groq call volume, token consumption, cost, "
            + "or usage breakdown today or over the last seven days. Always use live tracked data instead of guessing.")
    public String getAiUsageStats() {
        try {
            AiUsageStatsDTO stats = aiUsageStatsService.getStats();
            return formatUsagePeriod("Hôm nay", stats.today()) + "\n"
                    + formatUsagePeriod("7 ngày gần nhất", stats.last7Days());
        } catch (Exception exception) {
            log.warn("Không thể lấy thống kê AI usage cho AI admin: {}", exception.getMessage());
            return "Không thể lấy thống kê sử dụng AI lúc này. Vui lòng thử lại sau.";
        }
    }

    // Định dạng số liệu dùng AI của một khoảng thời gian thành chuỗi dễ đọc cho AI diễn giải lại.
    private static String formatUsagePeriod(String label, AiUsageStatsDTO.PeriodStats period) {
        String breakdown = period.byCallType().isEmpty() ? "Không có"
                : period.byCallType().stream()
                .map(item -> "%s: %d calls, %d tokens, $%.6f".formatted(item.callType(),
                        item.totalCalls(), item.totalTokens(), item.totalCostUsd()))
                .reduce((left, right) -> left + "; " + right).orElse("Không có");
        return "%s — Tổng calls: %d | Tổng tokens: %d | Chi phí: $%.6f | Theo loại: %s"
                .formatted(label, period.totalCalls(), period.totalTokens(), period.totalCostUsd(), breakdown);
    }

    // AI gọi khi Admin muốn tìm tài liệu theo từ khoá/visibility/trạng thái kiểm duyệt/môn học.
    @Tool(description = "Use this tool when an ADMIN wants to find documents by title keyword or filter them by "
            + "visibility, moderation status, or subject name. Filters are optional; use it instead of inventing document records.")
    public String searchDocuments(String keyword, String visibility, String moderationStatus,
                                  String subjectName, Integer limit) {
        try {
            List<DocDocument> documents = docDocumentRepository.searchForAdminTool(
                    blankToNull(keyword), parseEnum(DocumentVisibility.class, visibility),
                    parseEnum(ModerationStatus.class, moderationStatus), blankToNull(subjectName),
                    PageRequest.of(0, normalizeLimit(limit)));
            if (documents.isEmpty()) return "Không tìm thấy tài liệu phù hợp.";
            return documents.stream().map(document -> "ID %d | %s | Chủ sở hữu: %s | visibility: %s | moderationStatus: %s | ingestStatus: %s"
                            .formatted(document.getId(), value(document.getTitle()),
                                    value(document.getUser().getFullName()), value(document.getVisibility()),
                                    value(document.getModerationStatus()), value(document.getIngestStatus())))
                    .reduce((left, right) -> left + "\n" + right).orElseThrow();
        } catch (Exception exception) {
            log.warn("Không thể tìm tài liệu cho AI admin: {}", exception.getMessage());
            return "Không thể tìm tài liệu lúc này. Vui lòng thử lại sau.";
        }
    }

    // AI gọi khi Admin muốn tìm người dùng theo tên/email, lọc theo vai trò/trạng thái tài khoản.
    // Không bao giờ trả về mật khẩu hay thông tin đăng nhập nhạy cảm khác.
    @Tool(description = "Use this tool when an ADMIN wants to find users by name or email, optionally filtered by "
            + "role or account status. Never infer or expose credentials.")
    public String searchUsers(String keyword, String role, String status, Integer limit) {
        try {
            String normalizedRole = recognizedRole(role);
            List<AuthAccount> accounts = authAccountRepository.searchUsersForAdminTool(
                    blankToNull(keyword), normalizedRole, parseEnum(UserStatus.class, status),
                    PageRequest.of(0, normalizeLimit(limit)));
            if (accounts.isEmpty()) return "Không tìm thấy người dùng phù hợp.";
            return accounts.stream().map(account -> formatUser(account.getUser(), account.getIdentifier()))
                    .reduce((left, right) -> left + "\n" + right).orElseThrow();
        } catch (Exception exception) {
            log.warn("Không thể tìm người dùng cho AI admin: {}", exception.getMessage());
            return "Không thể tìm người dùng lúc này. Vui lòng thử lại sau.";
        }
    }

    // AI gọi khi Admin hỏi chi tiết trạng thái một tài liệu cụ thể theo ID.
    @Tool(description = "Use this tool when an ADMIN asks for the complete current status of one document by its numeric ID, "
            + "including moderation, ingest, metadata-match, review, and trash status.")
    public String getDocumentStatus(Long documentId) {
        try {
            if (documentId == null) return "Vui lòng cung cấp ID tài liệu.";
            Optional<DocDocument> found = docDocumentRepository.findStatusByIdForAdminTool(documentId);
            if (found.isEmpty()) return "Không tìm thấy tài liệu ID " + documentId + ".";
            DocDocument document = found.get();
            return """
                    ID: %d
                    Tiêu đề: %s
                    Chủ sở hữu: %s
                    Trạng thái xóa: %s
                    Visibility: %s
                    Moderation status: %s
                    Lý do kiểm duyệt: %s
                    Admin reviewed at: %s
                    Admin reviewed by: %s
                    Ingest status: %s
                    Metadata match status: %s
                    Metadata checked at: %s
                    Created at: %s""".formatted(
                    document.getId(), value(document.getTitle()), value(document.getUser().getFullName()),
                    document.getDeletedAt() == null ? "Không ở trong thùng rác" : "ĐÃ XÓA MỀM - đang ở trong thùng rác",
                    value(document.getVisibility()), value(document.getModerationStatus()),
                    value(document.getModerationReason()), value(document.getAdminReviewedAt()),
                    value(document.getAdminReviewedBy()), value(document.getIngestStatus()),
                    value(document.getMetadataMatchStatus()), value(document.getMetadataCheckedAt()),
                    value(document.getCreatedAt()));
        } catch (Exception exception) {
            log.warn("Không thể lấy trạng thái tài liệu {} cho AI admin: {}", documentId, exception.getMessage());
            return "Không thể lấy trạng thái tài liệu lúc này. Vui lòng thử lại sau.";
        }
    }

    // AI gọi khi Admin hỏi trạng thái một người dùng cụ thể theo ID số hoặc email.
    @Tool(description = "Use this tool when an ADMIN asks for the current status of one user identified by numeric user ID "
            + "or email, including role, status, and owned-document count.")
    public String getUserStatus(String identifier) {
        try {
            String normalizedIdentifier = blankToNull(identifier);
            if (normalizedIdentifier == null) return "Vui lòng cung cấp ID hoặc email người dùng.";

            AuthUser user;
            String email;
            try {
                // Thử coi identifier là ID số trước...
                Long userId = Long.valueOf(normalizedIdentifier);
                Optional<AuthUser> foundUser = authUserRepository.findById(userId);
                if (foundUser.isEmpty()) return "Không tìm thấy người dùng '" + normalizedIdentifier + "'.";
                user = foundUser.get();
                email = authAccountRepository.findFirstByUser_IdAndIsPrimaryTrue(userId)
                        .map(AuthAccount::getIdentifier).orElse("-");
            } catch (NumberFormatException ignored) {
                // ...không phải số thì coi là email, tìm khớp chính xác (không phân biệt hoa thường).
                Optional<AuthAccount> foundAccount = authAccountRepository.searchUsersForAdminTool(
                                normalizedIdentifier, null, null, PageRequest.of(0, 10)).stream()
                        .filter(account -> normalizedIdentifier.equalsIgnoreCase(account.getIdentifier()))
                        .findFirst();
                if (foundAccount.isEmpty()) return "Không tìm thấy người dùng '" + normalizedIdentifier + "'.";
                user = foundAccount.get().getUser();
                email = foundAccount.get().getIdentifier();
            }
            return formatUser(user, email) + " | Số tài liệu sở hữu: "
                    + docDocumentRepository.countByUser_Id(user.getId());
        } catch (Exception exception) {
            log.warn("Không thể lấy trạng thái người dùng cho AI admin: {}", exception.getMessage());
            return "Không thể lấy trạng thái người dùng lúc này. Vui lòng thử lại sau.";
        }
    }

    // Định dạng thông tin cơ bản của một user thành chuỗi dễ đọc cho AI diễn giải.
    private static String formatUser(AuthUser user, String email) {
        return "ID %d | %s | Email: %s | Vai trò: %s | Trạng thái: %s".formatted(
                user.getId(), value(user.getFullName()), value(email),
                user.getRole() == null ? "-" : value(user.getRole().getRoleName()), value(user.getStatus()));
    }

    private static int normalizeLimit(Integer limit) {
        return limit == null ? 5 : Math.max(1, Math.min(limit, 10));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static String recognizedRole(String role) {
        String normalized = blankToNull(role);
        if (normalized == null) return null;
        String upper = normalized.toUpperCase(Locale.ROOT);
        return "ADMIN".equals(upper) || "USER".equals(upper) ? upper : null;
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String raw) {
        String normalized = blankToNull(raw);
        if (normalized == null) return null;
        try {
            return Enum.valueOf(type, normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String value(Object value) {
        return value == null || value.toString().isBlank() ? "-" : value.toString();
    }
}
