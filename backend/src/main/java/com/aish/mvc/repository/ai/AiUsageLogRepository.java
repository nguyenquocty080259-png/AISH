package com.aish.mvc.repository.ai;

import com.aish.mvc.entity.ai.AiUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AiUsageLogRepository extends JpaRepository<AiUsageLog, Long> {
    @Query("SELECT COUNT(l), COALESCE(SUM(l.totalTokens), 0), COALESCE(SUM(l.costUsd), 0.0) " +
            "FROM AiUsageLog l WHERE l.createdAt >= :from AND l.createdAt < :to")
    List<Object[]> aggregateTotals(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT l.callType, COUNT(l), COALESCE(SUM(l.totalTokens), 0), " +
            "COALESCE(SUM(l.costUsd), 0.0) FROM AiUsageLog l " +
            "WHERE l.createdAt >= :from AND l.createdAt < :to GROUP BY l.callType ORDER BY l.callType")
    List<Object[]> aggregateByCallType(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // granularity là bind-param nhưng LUÔN đi qua UsageGranularity.toPgUnit() ở service trước khi
    // tới đây - không nhận chuỗi thô từ request, nên an toàn dù bind vào date_trunc().
    @Query(value = "SELECT date_trunc(:granularity, created_at) AS bucket, COUNT(*), " +
            "COALESCE(SUM(total_tokens), 0) FROM ai_usage_logs " +
            "WHERE created_at >= :from AND created_at < :to GROUP BY bucket ORDER BY bucket",
            nativeQuery = true)
    List<Object[]> aggregateByBucket(@Param("granularity") String granularity,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // AiChatService: đếm/tổng usage chat (CHAT_RAG/CHAT_GENERAL) của 1 user từ 1 mốc thời gian -
    // dùng để enforce quota lượt/token chat mỗi ngày (AIU-4).
    @Query("SELECT COUNT(l) FROM AiUsageLog l " +
            "WHERE l.userId = :uid AND l.callType IN :types AND l.createdAt >= :from")
    long countChatCallsForUserSince(@Param("uid") Long uid, @Param("types") Collection<String> types,
            @Param("from") LocalDateTime from);

    @Query("SELECT COALESCE(SUM(l.totalTokens), 0) FROM AiUsageLog l " +
            "WHERE l.userId = :uid AND l.callType IN :types AND l.createdAt >= :from")
    long sumChatTokensForUserSince(@Param("uid") Long uid, @Param("types") Collection<String> types,
            @Param("from") LocalDateTime from);
}
