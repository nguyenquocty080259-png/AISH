package com.aish.mvc.service.ai;

import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.entity.report.Report;
import com.aish.mvc.repository.doc.CollectionItemRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.FavoriteRepository;
import com.aish.mvc.repository.report.ReportRepository;
import com.aish.mvc.service.doc.DocumentAccessPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserAiTools {
    private static final Logger log = LoggerFactory.getLogger(UserAiTools.class);
    private static final String LOGIN_REQUIRED = "Bạn cần đăng nhập để xem dữ liệu cá nhân.";

    private final AiConversationService aiConversationService;
    private final DocDocumentRepository docDocumentRepository;
    private final FavoriteRepository favoriteRepository;
    private final CollectionItemRepository collectionItemRepository;
    private final ReportRepository reportRepository;
    private final DocumentAccessPort documentAccessPort;

    @Tool(description = "Use this tool when the logged-in person asks for their own document totals, visibility and "
            + "moderation breakdown, favorites received, or reports filed. Identity is resolved automatically.")
    @Transactional(readOnly = true)
    public String getMyStats() {
        try {
            AuthUser user = currentUser();
            if (user == null) return LOGIN_REQUIRED;
            List<DocDocument> documents = docDocumentRepository.findByDeletedAtIsNullAndUser_Id(user.getId());
            long publicCount = documents.stream().filter(d -> d.getVisibility() == DocumentVisibility.PUBLIC).count();
            long privateCount = documents.stream().filter(d -> d.getVisibility() == DocumentVisibility.PRIVATE).count();
            String moderationBreakdown = java.util.Arrays.stream(ModerationStatus.values())
                    .map(status -> status + ": " + documents.stream()
                            .filter(document -> document.getModerationStatus() == status).count())
                    .reduce((left, right) -> left + ", " + right).orElse("-");
            return """
                    ID người dùng: %d
                    Tổng tài liệu sở hữu (không tính thùng rác): %d
                    Công khai: %d
                    Riêng tư: %d
                    Phân loại kiểm duyệt: %s
                    Tổng lượt yêu thích nhận được: %d
                    Số report đã gửi: %d""".formatted(
                    user.getId(), documents.size(), publicCount, privateCount, moderationBreakdown,
                    favoriteRepository.countReceivedByDocumentOwner(user.getId()),
                    reportRepository.countByReporterUserId(user.getId()));
        } catch (Exception exception) {
            log.warn("Không thể lấy thống kê cá nhân cho AI user: {}", exception.getMessage());
            return "Không thể lấy thống kê cá nhân lúc này. Vui lòng thử lại sau.";
        }
    }

    @Tool(description = "Use this tool when the logged-in person wants to search documents they can access. It searches "
            + "their own, favorites, collections, then approved public documents; identity is resolved automatically.")
    @Transactional(readOnly = true)
    public String searchMyDocuments(String keyword, Integer limit) {
        try {
            AuthUser user = currentUser();
            if (user == null) return LOGIN_REQUIRED;
            String normalizedKeyword = blankToNull(keyword);
            int normalizedLimit = normalizeLimit(limit);
            Set<Long> included = new LinkedHashSet<>();
            List<String> lines = new ArrayList<>();

            addDocuments(docDocumentRepository.findByDeletedAtIsNullAndUser_Id(user.getId()),
                    "Tài liệu của tôi", normalizedKeyword, normalizedLimit, user.getId(), included, lines);
            addDocuments(docDocumentRepository.findAllById(favoriteRepository.findDocumentIdsByUserId(user.getId())),
                    "Yêu thích", normalizedKeyword, normalizedLimit, user.getId(), included, lines);
            addDocuments(docDocumentRepository.findAllById(collectionItemRepository.findDocumentIdsByCollectionOwner(user.getId())),
                    "Trong collection", normalizedKeyword, normalizedLimit, user.getId(), included, lines);
            addDocuments(docDocumentRepository.findPublicApprovedDocuments(
                            DocumentVisibility.PUBLIC, ModerationStatus.APPROVED),
                    "Công khai", normalizedKeyword, normalizedLimit, user.getId(), included, lines);

            return lines.isEmpty() ? "Không tìm thấy tài liệu phù hợp mà bạn có quyền truy cập."
                    : String.join("\n", lines);
        } catch (Exception exception) {
            log.warn("Không thể tìm tài liệu cá nhân cho AI user: {}", exception.getMessage());
            return "Không thể tìm tài liệu của bạn lúc này. Vui lòng thử lại sau.";
        }
    }

    @Tool(description = "Use this tool when the logged-in person asks for the status of one of their own documents by "
            + "numeric document ID. Never use it to inspect another person's document.")
    @Transactional(readOnly = true)
    public String getMyDocumentStatus(Long documentId) {
        try {
            AuthUser user = currentUser();
            if (user == null) return LOGIN_REQUIRED;
            String refusal = ownedDocumentNotFound(documentId);
            if (documentId == null) return refusal;
            DocDocument document = docDocumentRepository.findById(documentId).orElse(null);
            if (document == null || document.getUser() == null
                    || !user.getId().equals(document.getUser().getId())) return refusal;
            return """
                    ID: %d
                    Tiêu đề: %s
                    Visibility: %s
                    Moderation status: %s
                    Lý do kiểm duyệt: %s
                    Ingest status: %s
                    Metadata match status: %s
                    Admin xác nhận: %s
                    Created at: %s
                    Trạng thái thùng rác: %s""".formatted(
                    document.getId(), value(document.getTitle()), value(document.getVisibility()),
                    value(document.getModerationStatus()), value(document.getModerationReason()),
                    value(document.getIngestStatus()), value(document.getMetadataMatchStatus()),
                    document.getAdminReviewedAt() == null ? "Chưa được Admin xác nhận" : "Đã được Admin xác nhận",
                    value(document.getCreatedAt()), document.getDeletedAt() == null ? "Không" : "Đang ở trong thùng rác");
        } catch (Exception exception) {
            log.warn("Không thể lấy trạng thái tài liệu cá nhân {}: {}", documentId, exception.getMessage());
            return "Không thể lấy trạng thái tài liệu của bạn lúc này. Vui lòng thử lại sau.";
        }
    }

    @Tool(description = "Use this tool when the logged-in person asks about reports they filed and their processing "
            + "status. Identity is resolved automatically; return at most the ten most recent reports.")
    @Transactional(readOnly = true)
    public String getMyReportStatus() {
        try {
            AuthUser user = currentUser();
            if (user == null) return LOGIN_REQUIRED;
            List<Report> reports = reportRepository.findByReporterUserIdOrderByCreatedAtDesc(user.getId()).stream()
                    .limit(10).toList();
            if (reports.isEmpty()) return "Bạn chưa gửi report nào.";
            return reports.stream().map(report -> "ID %d | Loại/đối tượng: %s ID %s (nguồn: %s) | Trạng thái: %s | Created at: %s"
                            .formatted(report.getId(), value(report.getTargetType()), value(report.getTargetId()),
                                    value(report.getSource()), value(report.getStatus()), value(report.getCreatedAt())))
                    .reduce((left, right) -> left + "\n" + right).orElseThrow();
        } catch (Exception exception) {
            log.warn("Không thể lấy report cá nhân cho AI user: {}", exception.getMessage());
            return "Không thể lấy trạng thái report của bạn lúc này. Vui lòng thử lại sau.";
        }
    }

    private AuthUser currentUser() {
        return aiConversationService.currentUserOrNull();
    }

    private void addDocuments(List<DocDocument> documents, String group, String keyword, int limit,
                              Long userId, Set<Long> included, List<String> lines) {
        if (lines.size() >= limit) return;
        for (DocDocument document : documents) {
            if (lines.size() >= limit) return;
            if (document == null || document.getId() == null || included.contains(document.getId())) continue;
            if (!titleMatches(document.getTitle(), keyword)) continue;
            if ("Công khai".equals(group) && (document.getVisibility() != DocumentVisibility.PUBLIC
                    || document.getModerationStatus() != ModerationStatus.APPROVED)) continue;
            if (!documentAccessPort.isAvailableTo(document.getId(), userId)) continue;
            included.add(document.getId());
            lines.add("ID %d | %s | Chủ sở hữu: %s | Nhóm: %s".formatted(
                    document.getId(), value(document.getTitle()),
                    document.getUser() == null ? "-" : value(document.getUser().getFullName()), group));
        }
    }

    private static boolean titleMatches(String title, String keyword) {
        return keyword == null || title != null
                && title.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private static String ownedDocumentNotFound(Long documentId) {
        return "Không tìm thấy tài liệu ID " + value(documentId) + " trong tài liệu của bạn.";
    }

    private static int normalizeLimit(Integer limit) {
        return limit == null ? 5 : Math.max(1, Math.min(limit, 10));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static String value(Object value) {
        return value == null || value.toString().isBlank() ? "-" : value.toString();
    }
}
