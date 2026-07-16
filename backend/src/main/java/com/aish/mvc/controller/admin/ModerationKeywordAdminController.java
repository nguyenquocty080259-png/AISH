package com.aish.mvc.controller.admin;

import com.aish.mvc.dto.doc.ModerationKeywordCreateRequestDTO;
import com.aish.mvc.dto.doc.ModerationKeywordResponseDTO;
import com.aish.mvc.dto.doc.ModerationKeywordUpdateRequestDTO;
import com.aish.mvc.entity.enums.ModerationKeywordType;
import com.aish.mvc.service.doc.ModerationKeywordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/moderation-keywords")
@RequiredArgsConstructor
public class ModerationKeywordAdminController {

    private final ModerationKeywordService moderationKeywordService;

    @GetMapping
    public ResponseEntity<List<ModerationKeywordResponseDTO>> list(
            @RequestParam(required = false) ModerationKeywordType type) {
        return ResponseEntity.ok(moderationKeywordService.list(type));
    }

    @PostMapping
    public ResponseEntity<ModerationKeywordResponseDTO> create(
            @Valid @RequestBody ModerationKeywordCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(moderationKeywordService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModerationKeywordResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ModerationKeywordUpdateRequestDTO request) {
        return ResponseEntity.ok(moderationKeywordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        moderationKeywordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
