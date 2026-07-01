package com.aish.mvc.controller.ai;

import com.aish.mvc.dto.ai.ModerationResultDTO;
import com.aish.mvc.service.ai.AiModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/ai/moderate/{documentId} — chạy screen() và trả về quyết định, dùng để
 * test/demo độc lập với luồng publish. KHÔNG thay đổi visibility/moderationStatus của
 * tài liệu — chỉ trả kết quả. Yêu cầu đăng nhập (không nằm trong permitAll của
 * SecurityConfig nên mặc định rơi vào .anyRequest().authenticated()).
 */
@RestController
@RequestMapping("/api/ai/moderate")
@RequiredArgsConstructor
public class AiModerationController {

    private final AiModerationService aiModerationService;

    @PostMapping("/{documentId}")
    public ResponseEntity<ModerationResultDTO> moderate(@PathVariable Long documentId) {
        return ResponseEntity.ok(aiModerationService.screen(documentId));
    }
}
