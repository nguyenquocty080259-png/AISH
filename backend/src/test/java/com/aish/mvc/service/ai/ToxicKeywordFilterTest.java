package com.aish.mvc.service.ai;

import com.aish.mvc.entity.doc.ModerationKeyword;
import com.aish.mvc.entity.enums.ModerationKeywordType;
import com.aish.mvc.repository.doc.ModerationKeywordRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ToxicKeywordFilterTest {

    @Test
    void matchesActiveDatabaseKeywordCaseInsensitively() {
        ModerationKeywordRepository repository = mock(ModerationKeywordRepository.class);
        when(repository.findByTypeAndActiveTrue(ModerationKeywordType.AI_CHAT))
                .thenReturn(List.of(ModerationKeyword.builder().keyword("đồ ngu").build()));
        ToxicKeywordFilter filter = new ToxicKeywordFilter(repository);

        assertTrue(filter.containsSuspiciousKeyword("ĐỒ NGU thật đấy"));
        assertFalse(filter.containsSuspiciousKeyword("một câu bình thường"));
    }

    @Test
    void returnsFalseWhenKeywordLoadingFails() {
        ModerationKeywordRepository repository = mock(ModerationKeywordRepository.class);
        when(repository.findByTypeAndActiveTrue(ModerationKeywordType.AI_CHAT))
                .thenThrow(new RuntimeException("database unavailable"));
        ToxicKeywordFilter filter = new ToxicKeywordFilter(repository);

        assertFalse(filter.containsSuspiciousKeyword("đồ ngu"));
    }
}
