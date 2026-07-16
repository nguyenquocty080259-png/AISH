package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.Comment;
import com.aish.mvc.entity.enums.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByDocumentIdOrderByCreatedAtDesc(Long documentId);

    @EntityGraph(attributePaths = {"user", "document"})
    List<Comment> findByStatusOrderByCreatedAtDesc(CommentStatus status);

    @EntityGraph(attributePaths = {"user", "document"})
    List<Comment> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"user", "document"})
    Optional<Comment> findWithUserAndDocumentById(Long id);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Comment c
               set c.status = com.aish.mvc.entity.enums.CommentStatus.PENDING_REVIEW,
                   c.moderationReason = :reason,
                   c.disputeNote = null,
                   c.reviewedBy = null,
                   c.reviewedAt = null
             where c.id = :id
               and c.status = com.aish.mvc.entity.enums.CommentStatus.VISIBLE
               and c.content = :content
            """)
    int markPendingAfterAiFlag(
            @Param("id") Long id,
            @Param("content") String content,
            @Param("reason") String reason);

    @Transactional
    @Modifying
    void deleteByDocumentId(Long documentId);
}
