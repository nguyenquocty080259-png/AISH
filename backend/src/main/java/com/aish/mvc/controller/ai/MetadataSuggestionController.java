package com.aish.mvc.controller.ai;

import com.aish.mvc.dto.ai.MetadataSuggestionDTO;
import com.aish.mvc.service.ai.MetadataSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
