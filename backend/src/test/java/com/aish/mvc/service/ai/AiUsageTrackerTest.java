package com.aish.mvc.service.ai;

import com.aish.mvc.entity.ai.AiModel;
import com.aish.mvc.entity.ai.AiUsageLog;
import com.aish.mvc.repository.ai.AiModelRepository;
import com.aish.mvc.repository.ai.AiUsageLogRepository;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiUsageTrackerTest {
    private AiUsageLogRepository logs;
    private AiModelRepository models;
    private AuthAccountRepository accounts;
    private AiUsageTracker tracker;
    private AiModel model;

    @BeforeEach
    void setUp() {
        logs = mock(AiUsageLogRepository.class);
        models = mock(AiModelRepository.class);
        accounts = mock(AuthAccountRepository.class);
        tracker = new AiUsageTracker(logs, models, accounts);
        ReflectionTestUtils.setField(tracker, "configuredChatModel", "llama-3.3-70b-versatile");
        model = AiModel.builder().id(1L).modelKey("llama-3.3-70b-versatile")
                .displayName("Llama 3.3 70B (Groq)").inputPricePer1m(0.59)
                .outputPricePer1m(0.79).build();
        when(models.findByModelKey("llama-3.3-70b-versatile")).thenReturn(Optional.of(model));
    }

    @Test
    void calculatesAndPersistsTokenCost() {
        ChatResponse response = responseWithUsage(1_000, 500);

        tracker.log("CHAT_GENERAL", response, null);

        ArgumentCaptor<AiUsageLog> captor = ArgumentCaptor.forClass(AiUsageLog.class);
        verify(logs).saveAndFlush(captor.capture());
        AiUsageLog saved = captor.getValue();
        assertEquals(1_000, saved.getInputTokens());
        assertEquals(500, saved.getOutputTokens());
        assertEquals(1_500, saved.getTotalTokens());
        assertEquals(0.000985D, saved.getCostUsd(), 0.000000001D);
    }

    @Test
    void missingOrZeroUsageStillWritesNullTokenFields() {
        tracker.log("TEXT_MODERATION", responseWithUsage(0, 0), null);

        ArgumentCaptor<AiUsageLog> captor = ArgumentCaptor.forClass(AiUsageLog.class);
        verify(logs).saveAndFlush(captor.capture());
        assertNull(captor.getValue().getInputTokens());
        assertNull(captor.getValue().getOutputTokens());
        assertNull(captor.getValue().getTotalTokens());
    }

    @Test
    void repositoryFailureNeverPropagates() {
        when(logs.saveAndFlush(any())).thenThrow(new RuntimeException("database unavailable"));

        assertDoesNotThrow(() -> tracker.log("CHAT_RAG", responseWithUsage(10, 5), null));
    }

    @Test
    void priceMissingMakesCostNull() {
        assertNull(AiUsageTracker.calculateCost(100, 50, null, 0.79));
        assertNull(AiUsageTracker.calculateCost(100, 50, 0.59, null));
    }

    private static ChatResponse responseWithUsage(int promptTokens, int completionTokens) {
        ChatResponse response = mock(ChatResponse.class);
        ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
        Usage usage = mock(Usage.class);
        when(response.getMetadata()).thenReturn(metadata);
        when(metadata.getUsage()).thenReturn(usage);
        when(usage.getPromptTokens()).thenReturn(promptTokens);
        when(usage.getCompletionTokens()).thenReturn(completionTokens);
        return response;
    }
}
