package com.aish.mvc.service.ai;

import com.aish.mvc.entity.enums.ModerationKeywordType;
import com.aish.mvc.repository.doc.ModerationKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * LỚP LỌC TỪ KHOÁ RẺ chạy TRƯỚC khi gọi AI — so văn bản với danh sách từ khoá cấm trong database
 * (Admin quản lý). Mục đích: bắt được các trường hợp rõ ràng mà không cần tốn tiền/thời gian gọi
 * AI. Lỗi khi đọc danh sách từ khoá thì coi như "không trúng" (fail-open), để không chặn oan
 * người dùng chỉ vì database lỗi.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ToxicKeywordFilter {

    private final ModerationKeywordRepository moderationKeywordRepository;

    // Kiểm tra riêng cho tin nhắn AI chat (loại từ khoá AI_CHAT).
    public boolean containsSuspiciousKeyword(String text) {
        return matches(text, ModerationKeywordType.AI_CHAT);
    }

    // Đầu vào: văn bản cần kiểm tra + loại từ khoá (NAMING/COMMENT/AI_CHAT/DOCUMENT_CONTENT).
    // Trả về: true nếu văn bản chứa ít nhất một từ khoá cấm loại đó (so không phân biệt hoa thường).
    public boolean matches(String text, ModerationKeywordType type) {
        if (text == null || text.isBlank()) {
            return false;
        }
        if (type == null) {
            return false;
        }

        try {
            String normalized = text.toLowerCase(Locale.ROOT);
            boolean matched = moderationKeywordRepository.findByTypeAndActiveTrue(type).stream()
                    .map(keyword -> keyword.getKeyword().toLowerCase(Locale.ROOT))
                    .anyMatch(normalized::contains);
            if (matched) {
                log.info("Moderation keyword pre-filter matched type {}", type);
            }
            return matched;
        } catch (Exception exception) {
            log.warn("Không thể tải từ khóa kiểm duyệt loại {}; bỏ qua bước lọc để luồng chính tiếp tục: {}",
                    type, exception.getMessage(), exception);
            return false;
        }
    }
}
