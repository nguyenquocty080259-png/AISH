package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocDocumentRepository extends JpaRepository<DocDocument, Long> {
    List<DocDocument> findByDeletedAtIsNull();
    List<DocDocument> findByDeletedAtIsNotNull();

    // Tài liệu hiển thị được: PUBLIC của mọi người HOẶC tài liệu của chính user đang xem
    @Query("SELECT DISTINCT d FROM DocDocument d LEFT JOIN FETCH d.tags WHERE d.deletedAt IS NULL " +
            "AND (d.visibility = :pub OR d.user.id = :userId)")
    List<DocDocument> findVisibleDocuments(@Param("pub") DocumentVisibility pub, @Param("userId") Long userId);
}