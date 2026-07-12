package com.aish.mvc.service.ai;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class ToxicKeywordFilter {

    private static final List<String> SUSPICIOUS_KEYWORDS = List.of(
            "đồ ngu", "ngu ngốc", "đồ khốn", "khốn nạn", "đồ chó",
            "con chó", "chết đi", "giết mày", "giết chết", "đánh chết",
            "cút đi", "đồ rác rưởi", "óc chó", "súc vật", "đĩ điếm",
            "fuck you", "fucking idiot", "stupid idiot", "piece of shit",
            "son of a bitch", "go kill yourself", "kill yourself", "i will kill you",
            "i'll kill you", "hate your race", "racial slur", "worthless trash"
    );

    public boolean containsSuspiciousKeyword(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalized = text.toLowerCase(Locale.ROOT);
        return SUSPICIOUS_KEYWORDS.stream().anyMatch(normalized::contains);
    }
}
