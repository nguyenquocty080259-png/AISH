package com.aish.mvc.controller.ai;

import com.aish.mvc.dto.ai.AiChatRequest;
import com.aish.mvc.dto.ai.AiChatResponse;
import com.aish.mvc.service.ai.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CỬA NGÕ API cho tính năng chat với AI. Chỉ có 1 endpoint duy nhất — toàn bộ logic chọn chế độ
 * RAG/GENERAL, quota, lưu lịch sử đều nằm ở {@link AiChatService}.
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    /**
     * Authenticated endpoint — login required
     * POST /api/ai/chat
     * Body: { "message": "Website này là gì?" }
     */
    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        AiChatResponse response = aiChatService.chat(request);
        return ResponseEntity.ok(response);
    }
}
