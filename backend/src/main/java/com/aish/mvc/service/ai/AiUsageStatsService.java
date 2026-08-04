package com.aish.mvc.service.ai;

import com.aish.mvc.dto.ai.AiUsageReportDTO;
import com.aish.mvc.dto.ai.AiUsageStatsDTO;
import com.aish.mvc.entity.enums.UsageGranularity;
import com.aish.mvc.repository.ai.AiUsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thống kê MỨC ĐỘ SỬ DỤNG AI (số lượt gọi, số token, chi phí) cho trang quản trị — dữ liệu lấy từ
 * bảng ai_usage_logs (được AiUsageTracker ghi lại mỗi lần gọi AI).
 */
@Service
@RequiredArgsConstructor
public class AiUsageStatsService {
    private final AiUsageLogRepository usageLogRepository;

    // Thống kê nhanh cho dashboard: hôm nay + 7 ngày gần nhất.
    @Transactional(readOnly = true)
    public AiUsageStatsDTO getStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        return new AiUsageStatsDTO(
                period(today.atStartOfDay(), now),
                period(today.minusDays(6).atStartOfDay(), now));
    }

    // Mặc định: granularity=MONTH, khoảng thời gian = cả năm hiện tại (1/1 năm nay đến 1/1 năm sau).
    @Transactional(readOnly = true)
    public AiUsageReportDTO getReport(UsageGranularity granularity, LocalDate from, LocalDate to) {
        UsageGranularity effectiveGranularity = granularity != null ? granularity : UsageGranularity.MONTH;
        LocalDate currentYearStart = LocalDate.now().withDayOfYear(1);
        LocalDate effectiveFrom = from != null ? from : currentYearStart;
        LocalDate effectiveTo = to != null ? to : currentYearStart.plusYears(1);

        List<Object[]> rows = usageLogRepository.aggregateByBucket(
                effectiveGranularity.toPgUnit(), effectiveFrom.atStartOfDay(), effectiveTo.atStartOfDay());

        long totalTokens = 0;
        long totalCalls = 0;
        List<AiUsageReportDTO.Bucket> buckets = new ArrayList<>();
        for (Object[] row : rows) {
            long calls = number(row[1]).longValue();
            long tokens = number(row[2]).longValue();
            totalCalls += calls;
            totalTokens += tokens;
            buckets.add(new AiUsageReportDTO.Bucket(bucketLabel(row[0]), tokens, calls));
        }

        return new AiUsageReportDTO(effectiveGranularity.name(), effectiveFrom, effectiveTo,
                totalTokens, totalCalls, buckets);
    }

    // Định dạng nhãn thời gian của mỗi "bucket" (ngày/tuần/tháng) trong biểu đồ.
    private static String bucketLabel(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toString();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toString();
        }
        return String.valueOf(value);
    }

    // Tổng hợp số liệu (tổng token, tổng lượt gọi, chi phí) trong một khoảng thời gian, kèm phân
    // rã theo từng loại lệnh gọi AI (chat, kiểm duyệt, gợi ý metadata...).
    private AiUsageStatsDTO.PeriodStats period(LocalDateTime from, LocalDateTime to) {
        List<Object[]> totals = usageLogRepository.aggregateTotals(from, to);
        Object[] total = totals.isEmpty() ? new Object[]{0L, 0L, 0.0} : totals.getFirst();
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
