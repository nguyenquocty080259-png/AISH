package com.aish.mvc.repository.ai;

import com.aish.mvc.entity.ai.AiModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiModelRepository extends JpaRepository<AiModel, Long> {
}
