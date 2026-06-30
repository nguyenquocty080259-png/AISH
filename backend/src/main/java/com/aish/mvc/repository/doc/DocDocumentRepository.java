package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface DocDocumentRepository extends JpaRepository<DocDocument, Long> {
    List<DocDocument> findByDeletedAtIsNull();
    List<DocDocument> findByDeletedAtIsNotNull();

    // Tài liệu đã ở thùng rác trước mốc thời gian (để dọn sau 30 ngày)
    List<DocDocument> findByDeletedAtBefore(LocalDateTime time);

    // Thùng rác CHỈ của user đang đăng nhập
    List<DocDocument> findByDeletedAtIsNotNullAndUser_Id(Long userId);

    @Query("SELECT DISTINCT d FROM DocDocument d LEFT JOIN FETCH d.subjects WHERE d.deletedAt IS NULL " +
            "AND (d.visibility = :pub OR d.user.id = :userId)")
    List<DocDocument> findVisibleDocuments(@Param("pub") DocumentVisibility pub, @Param("userId") Long userId);

    // Danh sách tài liệu công khai (trang Cộng đồng) - chưa bị xoá
    // CAST(:keyword AS string) để Postgres không hiểu nhầm tham số là bytea
    @Query("SELECT DISTINCT d FROM DocDocument d LEFT JOIN FETCH d.subjects " +
            "WHERE d.deletedAt IS NULL AND d.visibility = :pub " +
            "AND (:keyword IS NULL " +
            "     OR LOWER(d.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) " +
            "     OR LOWER(d.description) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) " +
            "AND (:subjectId IS NULL OR EXISTS (SELECT 1 FROM d.subjects s WHERE s.id = :subjectId))")
    List<DocDocument> findCommunityDocuments(@Param("pub") DocumentVisibility pub,
                                             @Param("keyword") String keyword,
                                             @Param("subjectId") Long subjectId);
}