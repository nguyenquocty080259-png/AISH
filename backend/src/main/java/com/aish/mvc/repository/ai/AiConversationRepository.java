package com.aish.mvc.repository.ai;

import com.aish.mvc.entity.ai.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    List<AiConversation> findByUser_IdOrderByUpdatedAtDescCreatedAtDesc(Long userId);

    Optional<AiConversation> findByIdAndUser_Id(Long id, Long userId);
}
