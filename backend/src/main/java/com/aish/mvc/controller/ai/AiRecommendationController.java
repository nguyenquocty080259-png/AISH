package com.aish.mvc.controller.ai;

import com.aish.mvc.dto.ai.RecommendedDocumentDTO;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.service.ai.AiRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Không nằm trong permitAll của SecurityConfig -> yêu cầu đăng nhập theo mặc định
 * (.anyRequest().authenticated()), kể cả khi gọi kèm documentId.
 */
@RestController
@RequestMapping("/api/ai/recommendations")
@RequiredArgsConstructor
public class AiRecommendationController {

    private static final int DEFAULT_LIMIT = 8;

    private final AiRecommendationService aiRecommendationService;
    private final AuthAccountRepository authAccountRepository;

    // documentId có -> "Liên quan tới tài liệu này". documentId không có -> "Dành cho bạn".
    @GetMapping
    public ResponseEntity<List<RecommendedDocumentDTO>> recommendations(
            @RequestParam(value = "documentId", required = false) Long documentId,
            @RequestParam(value = "limit", required = false, defaultValue = "8") int limit) {
        int safeLimit = limit > 0 ? Math.min(limit, 20) : DEFAULT_LIMIT;
        Long currentUserId = currentUserId();

        List<RecommendedDocumentDTO> result = documentId != null
                ? aiRecommendationService.recommendRelatedToDocument(documentId, currentUserId, safeLimit)
                : aiRecommendationService.recommendForUser(currentUserId, safeLimit);

        return ResponseEntity.ok(result);
    }

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .map(a -> a.getUser().getId())
                .orElse(null);
    }
}
