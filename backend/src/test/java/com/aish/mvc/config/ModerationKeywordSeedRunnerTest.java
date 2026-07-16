package com.aish.mvc.config;

import com.aish.mvc.entity.doc.ModerationKeyword;
import com.aish.mvc.entity.enums.ModerationKeywordType;
import com.aish.mvc.repository.doc.ModerationKeywordRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ModerationKeywordSeedRunnerTest {

    @Test
    void seedsExactlyTwentySevenAiChatKeywordsWhenTypeHasNoRows() {
        ModerationKeywordRepository repository = mock(ModerationKeywordRepository.class);
        when(repository.existsByType(ModerationKeywordType.AI_CHAT)).thenReturn(false);
        ModerationKeywordSeedRunner runner = new ModerationKeywordSeedRunner(repository);

        runner.run();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ModerationKeyword>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        assertEquals(27, captor.getValue().size());
        assertTrue(captor.getValue().stream().allMatch(keyword ->
                keyword.getType() == ModerationKeywordType.AI_CHAT && keyword.isActive()));
    }

    @Test
    void skipsSeedWhenAnyAiChatRowAlreadyExists() {
        ModerationKeywordRepository repository = mock(ModerationKeywordRepository.class);
        when(repository.existsByType(ModerationKeywordType.AI_CHAT)).thenReturn(true);
        ModerationKeywordSeedRunner runner = new ModerationKeywordSeedRunner(repository);

        runner.run();

        verify(repository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }
}
