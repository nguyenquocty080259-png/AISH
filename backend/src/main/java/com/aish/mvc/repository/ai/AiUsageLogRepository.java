package com.aish.mvc.repository.ai;

import com.aish.mvc.entity.ai.AiUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiUsageLogRepository extends JpaRepository<AiUsageLog, Long> {
}
