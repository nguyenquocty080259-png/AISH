package com.aish.mvc.repository.ai;

import com.aish.mvc.entity.ai.AiUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AiUsageLogRepository extends JpaRepository<AiUsageLog, Long> {
    @Query("SELECT COUNT(l), COALESCE(SUM(l.totalTokens), 0), COALESCE(SUM(l.costUsd), 0.0) " +
            "FROM AiUsageLog l WHERE l.createdAt >= :from AND l.createdAt < :to")
    List<Object[]> aggregateTotals(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT l.callType, COUNT(l), COALESCE(SUM(l.totalTokens), 0), " +
            "COALESCE(SUM(l.costUsd), 0.0) FROM AiUsageLog l " +
            "WHERE l.createdAt >= :from AND l.createdAt < :to GROUP BY l.callType ORDER BY l.callType")
    List<Object[]> aggregateByCallType(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
