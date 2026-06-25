package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    // Đếm tổng số lượt tải
    Long countByDocumentId(Long documentId);

    // Tính điểm trung bình số sao đánh giá (Nếu chưa có ai đánh giá thì trả về 0.0)
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Rating r WHERE r.document.id = :documentId")
    Double getAverageRatingByDocumentId(@Param("documentId") Long documentId);
}