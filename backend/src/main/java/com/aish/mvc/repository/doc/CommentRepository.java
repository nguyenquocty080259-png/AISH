package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Lấy danh sách bình luận của tài liệu xếp theo thời gian mới nhất
    List<Comment> findByDocumentIdOrderByCreatedAtDesc(Long documentId);
}