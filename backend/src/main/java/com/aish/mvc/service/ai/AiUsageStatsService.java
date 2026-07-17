package com.aish.mvc.service.ai;

import com.aish.mvc.dto.ai.AiUsageStatsDTO;
import com.aish.mvc.repository.ai.AiUsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiUsageStatsService {
    private final AiUsageLogRepository usageLogRepository;

    @Transactional(readOnly = true)
    public AiUsageStatsDTO getStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        return new AiUsageStatsDTO(
                period(today.atStartOfDay(), now),
                period(today.minusDays(6).atStartOfDay(), now));
    }

    private AiUsageStatsDTO.PeriodStats period(LocalDateTime from, LocalDateTime to) {
        Object[] total = usageLogRepository.aggregateTotals(from, to);
        List<AiUsageStatsDTO.CallTypeStats> breakdown = usageLogRepository
                .aggregateByCallType(from, to).stream()
                .map(row -> new AiUsageStatsDTO.CallTypeStats((String) row[0], number(row[1]).longValue(),
                        number(row[2]).longValue(), number(row[3]).doubleValue()))
                .toList();
        return new AiUsageStatsDTO.PeriodStats(number(total[0]).longValue(),
                number(total[1]).longValue(), number(total[2]).doubleValue(), breakdown);
    }

    private static Number number(Object value) {
        return value instanceof Number number ? number : 0;
    }
}
