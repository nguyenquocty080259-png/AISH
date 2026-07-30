package com.aish.mvc.controller.admin;

import com.aish.mvc.dto.ai.AiUsageReportDTO;
import com.aish.mvc.dto.ai.AiUsageStatsDTO;
import com.aish.mvc.entity.enums.UsageGranularity;
import com.aish.mvc.service.ai.AiUsageStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/ai-usage")
@RequiredArgsConstructor
public class AiUsageAdminController {
    private final AiUsageStatsService aiUsageStatsService;

    @GetMapping
    public ResponseEntity<AiUsageStatsDTO> getAiUsage() {
        return ResponseEntity.ok(aiUsageStatsService.getStats());
    }

    @GetMapping("/report")
    public ResponseEntity<AiUsageReportDTO> getReport(
            @RequestParam(required = false) String granularity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(aiUsageStatsService.getReport(parseGranularity(granularity), from, to));
    }

    private UsageGranularity parseGranularity(String granularity) {
        if (granularity == null || granularity.isBlank()) {
            return null;
        }
        try {
            return UsageGranularity.valueOf(granularity.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("error.aiUsage.invalidGranularity");
        }
    }
}
