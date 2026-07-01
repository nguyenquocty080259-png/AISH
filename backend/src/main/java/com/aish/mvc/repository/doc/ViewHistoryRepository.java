package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.ViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {

    Optional<ViewHistory> findByUser_IdAndDocumentId(Long userId, Long documentId);

    // Xem gần đây nhất trước. Lấy dư (>20) rồi mới lọc availability ở service để đủ 20 doc khả dụng.
    List<ViewHistory> findByUser_IdOrderByViewedAtDesc(Long userId);
}
