package com.aish.mvc.dto.ai;

import java.time.LocalDate;
import java.util.List;

// Báo cáo token theo mốc thời gian cho admin - chỉ token + calls, KHÔNG có cost (khác AiUsageStatsDTO).
public record AiUsageReportDTO(String granularity, LocalDate from, LocalDate to,
                                long totalTokens, long totalCalls, List<Bucket> buckets) {
    public record Bucket(String bucket, long tokens, long calls) {}
}
