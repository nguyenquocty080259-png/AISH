package com.aish.mvc.repository.ai;

import com.aish.mvc.entity.ai.AiModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiModelRepository extends JpaRepository<AiModel, Long> {
    Optional<AiModel> findByModelKey(String modelKey);
    Optional<AiModel> findFirstByIsActiveTrueOrderByIdAsc();
}
