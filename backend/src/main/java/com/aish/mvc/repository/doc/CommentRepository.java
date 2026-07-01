package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByDocumentIdOrderByCreatedAtDesc(Long documentId);

    @Transactional
    @Modifying
    void deleteByDocumentId(Long documentId);
}