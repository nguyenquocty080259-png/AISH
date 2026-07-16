package com.aish.mvc.config;

import com.aish.mvc.entity.doc.ModerationKeyword;
import com.aish.mvc.entity.enums.ModerationKeywordType;
import com.aish.mvc.repository.doc.ModerationKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class ModerationKeywordSeedRunner implements CommandLineRunner {

    private static final List<String> AI_CHAT_KEYWORDS = List.of(
            "đồ ngu", "ngu ngốc", "đồ khốn", "khốn nạn", "đồ chó",
            "con chó", "chết đi", "giết mày", "giết chết", "đánh chết",
            "cút đi", "đồ rác rưởi", "óc chó", "súc vật", "đĩ điếm",
            "fuck you", "fucking idiot", "stupid idiot", "piece of shit",
            "son of a bitch", "go kill yourself", "kill yourself", "i will kill you",
            "i'll kill you", "hate your race", "racial slur", "worthless trash"
    );

    private final ModerationKeywordRepository moderationKeywordRepository;

    @Override
    public void run(String... args) {
        if (moderationKeywordRepository.existsByType(ModerationKeywordType.AI_CHAT)) {
            log.info("AI chat moderation keywords already exist; skipping seed");
            return;
        }

        List<ModerationKeyword> keywords = AI_CHAT_KEYWORDS.stream()
                .map(keyword -> ModerationKeyword.builder()
                        .keyword(keyword)
                        .type(ModerationKeywordType.AI_CHAT)
                        .active(true)
                        .build())
                .toList();

        moderationKeywordRepository.saveAll(keywords);
        log.info("Seeded {} AI chat moderation keywords", keywords.size());
    }
}
