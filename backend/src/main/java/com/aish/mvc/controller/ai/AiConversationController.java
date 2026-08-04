package com.aish.mvc.controller.ai;

import com.aish.mvc.dto.ai.AiConversationSummaryDTO;
import com.aish.mvc.dto.ai.AiMessageDTO;
import com.aish.mvc.dto.ai.RenameConversationRequestDTO;
import com.aish.mvc.service.ai.AiConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CỬA NGÕ API quản lý LỊCH SỬ CHAT: danh sách cuộc trò chuyện, xem tin nhắn, đổi tên, xoá.
 * Mọi thao tác đều tính theo user đang đăng nhập ({@link AiConversationService} tự chặn nếu
 * cuộc trò chuyện không phải của người gọi).
 */
@RestController
@RequestMapping("/api/ai/conversations")
@RequiredArgsConstructor
public class AiConversationController {

    private final AiConversationService aiConversationService;

    // GET /api/ai/conversations — danh sách cuộc trò chuyện của tôi.
    @GetMapping
    public ResponseEntity<List<AiConversationSummaryDTO>> getConversations() {
        return ResponseEntity.ok(aiConversationService.getMyConversations());
    }

    // GET /api/ai/conversations/{id}/messages — toàn bộ tin nhắn của một cuộc trò chuyện.
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<AiMessageDTO>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(aiConversationService.getMyMessages(id));
    }

    // DELETE /api/ai/conversations/{id} — xoá cuộc trò chuyện.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id) {
        aiConversationService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/ai/conversations/{id} — đổi tên cuộc trò chuyện.
    @PutMapping("/{id}")
    public ResponseEntity<AiConversationSummaryDTO> renameConversation(
            @PathVariable Long id,
            @RequestBody RenameConversationRequestDTO request) {
        return ResponseEntity.ok(aiConversationService.renameConversation(id, request.getTitle()));
    }
}
