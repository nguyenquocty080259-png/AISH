package com.aish.mvc.controller.ai;

import com.aish.mvc.dto.ai.MetadataSuggestionDTO;
import com.aish.mvc.service.ai.MetadataSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * POST /api/ai/documents/{id}/metadata-suggestion — xin AI gợi ý tiêu đề/mô tả/môn học cho một
 * tài liệu đã ingest. Quyền hạn (chủ tài liệu/Admin) và điều kiện (đã ingest) được kiểm ở
 * {@link MetadataSuggestionService}.
 */
@RestController
@RequestMapping("/api/ai/documents")
@RequiredArgsConstructor
public class MetadataSuggestionController {
    private final MetadataSuggestionService service;

    @PostMapping("/{id}/metadata-suggestion")
    public ResponseEntity<MetadataSuggestionDTO> suggest(@PathVariable Long id) {
        return ResponseEntity.ok(service.suggest(id));
    }
}
