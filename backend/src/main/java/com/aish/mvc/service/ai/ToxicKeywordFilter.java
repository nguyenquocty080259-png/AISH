package com.aish.mvc.service.ai;

import com.aish.mvc.entity.enums.ModerationKeywordType;
import com.aish.mvc.repository.doc.ModerationKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class ToxicKeywordFilter {

    private final ModerationKeywordRepository moderationKeywordRepository;

    public boolean containsSuspiciousKeyword(String text) {
        return matches(text, ModerationKeywordType.AI_CHAT);
    }

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
