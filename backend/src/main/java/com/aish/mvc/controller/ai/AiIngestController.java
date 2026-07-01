package com.aish.mvc.controller.ai;

import com.aish.mvc.dto.ai.IngestResponseDTO;
import com.aish.mvc.service.doc.DocEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/ai/ingest/{documentId} — yêu cầu đăng nhập (không nằm trong danh sách
 * permitAll của SecurityConfig nên mặc định rơi vào .anyRequest().authenticated()).
 * Owner của tài liệu hoặc ROLE_ADMIN mới được ingest — kiểm tra trong DocEmbeddingService.
 */
@RestController
@RequestMapping("/api/ai/ingest")
@RequiredArgsConstructor
public class AiIngestController {

    private final DocEmbeddingService docEmbeddingService;

    @PostMapping("/{documentId}")
    public ResponseEntity<IngestResponseDTO> ingest(@PathVariable Long documentId) {
        return ResponseEntity.ok(docEmbeddingService.ingest(documentId));
    }
}
