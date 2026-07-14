package com.aish.mvc.repository.ai;

import com.aish.mvc.entity.ai.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    List<AiConversation> findByUser_IdOrderByUpdatedAtDescCreatedAtDesc(Long userId);

    Optional<AiConversation> findByIdAndUser_Id(Long id, Long userId);

    @Modifying
    @Query("UPDATE AiConversation a SET a.document = null WHERE a.document.id = :documentId")
    void clearDocumentReference(@Param("documentId") Long documentId);
}
