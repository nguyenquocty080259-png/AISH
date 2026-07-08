package com.aish.mvc.repository.ai;

import com.aish.mvc.entity.ai.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {
    List<AiMessage> findByConversation_IdOrderByOrderIndexAsc(Long conversationId);

    int countByConversation_Id(Long conversationId);
}
