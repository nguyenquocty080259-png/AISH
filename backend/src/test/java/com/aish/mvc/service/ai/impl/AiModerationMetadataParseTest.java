package com.aish.mvc.service.ai.impl;

import com.aish.mvc.dto.ai.ModerationDecision;
import com.aish.mvc.service.ai.AiContentSignalService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AiModerationMetadataParseTest {
    private final AiModerationServiceImpl service = new AiModerationServiceImpl(
            mock(com.aish.mvc.repository.doc.DocDocumentRepository.class),
            mock(AiContentSignalService.class), mock(ChatClient.class),
            mock(com.aish.mvc.service.ai.AiUsageTracker.class));

    @Test
    void malformedMetadataLinesFailOpenToMatch() {
        var result = service.parseResponse(1L, "PASS\nỔn\nUNKNOWN");
        assertEquals(ModerationDecision.PASS, result.getDecision());
        assertFalse(result.isMetadataMismatch());
    }

    @Test
    void validLechIsAdvisoryOnly() {
        var result = service.parseResponse(1L, "PASS\nNội dung ổn\nLECH\nTên không liên quan");
        assertEquals(ModerationDecision.PASS, result.getDecision());
        assertTrue(result.isMetadataMismatch());
        assertEquals("Tên không liên quan", result.getMetadataMismatchReason());
    }

    @Test
    void acceptsVietnameseDiacriticsAndMarkdownAroundToken() {
        var result = service.parseResponse(1L, "PASS\nỔn\n**LỆCH**\nTên không liên quan");
        assertTrue(result.isMetadataMismatch());
    }

    @Test
    void acceptsMergedMetadataReasonAfterLechToken() {
        var result = service.parseResponse(1L, "PASS\nỔn\nLỆCH - tiêu đề không liên quan");
        assertTrue(result.isMetadataMismatch());
        assertEquals("tiêu đề không liên quan", result.getMetadataMismatchReason());
    }
}
