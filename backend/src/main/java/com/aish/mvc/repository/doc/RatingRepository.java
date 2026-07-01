package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Long countByDocumentId(Long documentId);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Rating r WHERE r.document.id = :documentId")
    Double getAverageRatingByDocumentId(@Param("documentId") Long documentId);

    @Transactional
    @Modifying
    void deleteByDocumentId(Long documentId);
}