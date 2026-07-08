package com.aish.mvc.controller.ai;

import com.aish.mvc.dto.ai.AiConversationSummaryDTO;
import com.aish.mvc.dto.ai.AiMessageDTO;
import com.aish.mvc.service.ai.AiConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai/conversations")
@RequiredArgsConstructor
public class AiConversationController {

    private final AiConversationService aiConversationService;

    @GetMapping
    public ResponseEntity<List<AiConversationSummaryDTO>> getConversations() {
        return ResponseEntity.ok(aiConversationService.getMyConversations());
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<AiMessageDTO>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(aiConversationService.getMyMessages(id));
    }
}
