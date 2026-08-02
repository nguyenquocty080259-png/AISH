package com.aish.mvc.controller.doc;

import com.aish.mvc.dto.doc.RecommendedDocumentDTO;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.service.doc.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API GỢI Ý TÀI LIỆU (/api/ai/recommendations) — phục vụ cả hai chỗ: mục "Tài liệu liên quan" ở
 * trang chi tiết và mục "Dành cho bạn" ở trang chủ. Phân biệt bằng việc có truyền documentId hay không.
 *
 * <p>Không nằm trong permitAll của SecurityConfig -> yêu cầu đăng nhập theo mặc định
 * (.anyRequest().authenticated()), kể cả khi gọi kèm documentId.
 */
@RestController
@RequestMapping("/api/ai/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private static final int DEFAULT_LIMIT = 8;

    private final RecommendationService recommendationService;
    private final AuthAccountRepository authAccountRepository;

    // documentId có -> "Liên quan tới tài liệu này". documentId không có -> "Dành cho bạn".
    /**
     * Lấy danh sách tài liệu gợi ý.
     *
     * <p>Đầu vào: documentId (tuỳ chọn) và limit (số lượng muốn lấy). Trả về: danh sách tài liệu
     * kèm điểm gợi ý, xếp từ cao xuống thấp.
     */
    @GetMapping
    public ResponseEntity<List<RecommendedDocumentDTO>> recommendations(
            @RequestParam(value = "documentId", required = false) Long documentId,
            @RequestParam(value = "limit", required = false, defaultValue = "8") int limit) {
        // Kẹp limit trong khoảng hợp lý: số âm/0 -> mặc định 8, và trần 20 để một request
        // ?limit=100000 không bắt server chấm điểm cả kho tài liệu.
        int safeLimit = limit > 0 ? Math.min(limit, 20) : DEFAULT_LIMIT;
        Long currentUserId = currentUserId();

        List<RecommendedDocumentDTO> result = documentId != null
                ? recommendationService.recommendRelatedToDocument(documentId, currentUserId, safeLimit)
                : recommendationService.recommendForUser(currentUserId, safeLimit);

        return ResponseEntity.ok(result);
    }

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .map(a -> a.getUser().getId())
                .orElse(null);
    }
}
