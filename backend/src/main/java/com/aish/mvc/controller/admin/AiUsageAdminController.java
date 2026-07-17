package com.aish.mvc.controller.admin;

import com.aish.mvc.dto.ai.AiUsageStatsDTO;
import com.aish.mvc.service.ai.AiUsageStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ai-usage")
@RequiredArgsConstructor
public class AiUsageAdminController {
    private final AiUsageStatsService aiUsageStatsService;

    @GetMapping
    public ResponseEntity<AiUsageStatsDTO> getAiUsage() {
        return ResponseEntity.ok(aiUsageStatsService.getStats());
    }
}
