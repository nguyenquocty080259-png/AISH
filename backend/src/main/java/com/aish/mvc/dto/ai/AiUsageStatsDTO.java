package com.aish.mvc.dto.ai;

import java.util.List;

public record AiUsageStatsDTO(PeriodStats today, PeriodStats last7Days) {
    public record PeriodStats(long totalCalls, long totalTokens, double totalCostUsd,
                              List<CallTypeStats> byCallType) {}

    public record CallTypeStats(String callType, long totalCalls, long totalTokens,
                                double totalCostUsd) {}
}
