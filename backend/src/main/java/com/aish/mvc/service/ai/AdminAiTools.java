package com.aish.mvc.service.ai;

import com.aish.mvc.dto.doc.AdminStatsDTO;
import com.aish.mvc.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAiTools {

    private static final Logger log = LoggerFactory.getLogger(AdminAiTools.class);
    private static final String UNAVAILABLE_MESSAGE = "Không lấy được số liệu hệ thống lúc này.";

    private final AdminService adminService;

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
}
