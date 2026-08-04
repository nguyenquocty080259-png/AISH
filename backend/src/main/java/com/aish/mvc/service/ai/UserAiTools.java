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

/**
 * "CÔNG CỤ" mà AI Chat được phép TỰ GỌI (function calling / tool-use của Spring AI, đánh dấu
 * bằng @Tool) khi người dùng hỏi về dữ liệu CÁ NHÂN của chính họ: thống kê tài liệu, tìm tài liệu
 * mình có quyền xem, trạng thái kiểm duyệt một tài liệu, hoặc report đã gửi. AI đọc phần
 * "description" trong @Tool để tự quyết định khi nào cần gọi hàm nào — không phải gọi trực tiếp
 * từ controller. Mọi hàm đều tự suy ra "user hiện tại" từ token, KHÔNG bao giờ lộ dữ liệu người khác.
 */
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

    // AI gọi khi user hỏi "tôi có bao nhiêu tài liệu", "tài liệu của tôi được duyệt chưa"...
    // Trả về: chuỗi văn bản tóm tắt số liệu (AI sẽ diễn giải lại cho tự nhiên khi trả lời).
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
                    Tổng tài liệu sở hữu (không tính thùng rác): %d
                    Công khai: %d
                    Riêng tư: %d
                    Phân loại kiểm duyệt: %s
                    Tổng lượt yêu thích nhận được: %d
                    Số report đã gửi: %d""".formatted(
                    documents.size(), publicCount, privateCount, moderationBreakdown,
                    favoriteRepository.countReceivedByDocumentOwner(user.getId()),
                    reportRepository.countByReporterUserId(user.getId()));
        } catch (Exception exception) {
            log.warn("Không thể lấy thống kê cá nhân cho AI user: {}", exception.getMessage());
            return "Không thể lấy thống kê cá nhân lúc này. Vui lòng thử lại sau.";
        }
    }

    // AI gọi khi user muốn tìm tài liệu mà họ có quyền xem. Tìm lần lượt theo 4 nhóm: tài liệu
    // của mình -> yêu thích -> trong collection -> công khai đã duyệt; mỗi tài liệu chỉ liệt kê
    // một lần dù trùng ở nhiều nhóm (dựa vào tập "included").
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

    // AI gọi khi user hỏi trạng thái một tài liệu cụ thể của mình (theo tiêu đề/từ khoá).
    // Khớp nhiều hơn 1 tài liệu -> yêu cầu AI hỏi lại cho rõ thay vì đoán bừa.
    @Tool(description = "Use this tool when the logged-in person asks for the status of one of their own documents by "
            + "title or keyword. Never use it to inspect another person's document.")
    @Transactional(readOnly = true)
    public String getMyDocumentStatus(String titleOrKeyword) {
        try {
            AuthUser user = currentUser();
            if (user == null) return LOGIN_REQUIRED;
            String keyword = blankToNull(titleOrKeyword);
            if (keyword == null) return "Vui lòng cho biết tiêu đề hoặc từ khóa của tài liệu bạn muốn tra cứu.";

            List<DocDocument> matches = docDocumentRepository.findByDeletedAtIsNullAndUser_Id(user.getId()).stream()
                    .filter(document -> titleMatches(document.getTitle(), keyword))
                    .toList();

            if (matches.isEmpty()) {
                return "Không tìm thấy tài liệu nào của bạn khớp với \"" + keyword + "\".";
            }
            if (matches.size() > 1) {
                String titles = matches.stream().limit(5).map(document -> value(document.getTitle()))
                        .reduce((left, right) -> left + ", " + right).orElse("-");
                return "Có nhiều tài liệu của bạn khớp với \"" + keyword + "\": " + titles
                        + ". Bạn vui lòng nói rõ tiêu đề hơn.";
            }

            DocDocument document = matches.get(0);
            return """
                    Tiêu đề: %s
                    Visibility: %s
                    Moderation status: %s
                    Lý do kiểm duyệt: %s
                    Ingest status: %s
                    Metadata match status: %s
                    Admin xác nhận: %s
                    Created at: %s
                    Trạng thái thùng rác: %s""".formatted(
                    value(document.getTitle()), value(document.getVisibility()),
                    value(document.getModerationStatus()), value(document.getModerationReason()),
                    value(document.getIngestStatus()), value(document.getMetadataMatchStatus()),
                    document.getAdminReviewedAt() == null ? "Chưa được Admin xác nhận" : "Đã được Admin xác nhận",
                    value(document.getCreatedAt()), document.getDeletedAt() == null ? "Không" : "Đang ở trong thùng rác");
        } catch (Exception exception) {
            log.warn("Không thể lấy trạng thái tài liệu cá nhân theo tiêu đề '{}': {}", titleOrKeyword, exception.getMessage());
            return "Không thể lấy trạng thái tài liệu của bạn lúc này. Vui lòng thử lại sau.";
        }
    }

    // AI gọi khi user hỏi về report mình đã gửi và trạng thái xử lý — tối đa 10 report gần nhất.
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
            return reports.stream().map(report -> "Loại/đối tượng: %s (nguồn: %s) | Trạng thái: %s | Created at: %s"
                            .formatted(value(report.getTargetType()),
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

    // Lọc tài liệu theo từ khoá + còn quyền xem, gom vào "lines" (đủ limit dòng thì dừng sớm).
    // Nhóm "Công khai" còn lọc thêm: phải PUBLIC và đã APPROVED mới được liệt.
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
            lines.add("%s | Chủ sở hữu: %s | Nhóm: %s".formatted(
                    value(document.getTitle()),
                    document.getUser() == null ? "-" : value(document.getUser().getFullName()), group));
        }
    }

    private static boolean titleMatches(String title, String keyword) {
        return keyword == null || title != null
                && title.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
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
