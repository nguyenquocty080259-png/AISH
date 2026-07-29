package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.ModerationAppeal;
import com.aish.mvc.entity.enums.AppealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ModerationAppealRepository extends JpaRepository<ModerationAppeal, Long> {

    // Hàng chờ Admin: mọi appeal đang ở 1 trạng thái cho trước, cũ nhất trước.
    List<ModerationAppeal> findByStatusOrderByCreatedAtAsc(AppealStatus status);

    // Lịch sử kháng cáo của 1 tài liệu (mới nhất trước).
    List<ModerationAppeal> findByDocument_IdOrderByCreatedAtDesc(Long documentId);

    // Kháng cáo của 1 user (mới nhất trước) — dùng cho GET /api/appeals/mine.
    List<ModerationAppeal> findByUser_IdOrderByCreatedAtDesc(Long userId);

    long countByUser_IdAndStatus(Long userId, AppealStatus status);

    // Guard chống appeal trùng: 1 tài liệu chỉ được có tối đa 1 appeal đang PENDING tại 1 thời điểm.
    boolean existsByDocument_IdAndStatus(Long documentId, AppealStatus status);

    long countByStatus(AppealStatus status);

    // Dùng bởi DbSeedRunner khi dọn seed cũ.
    @Transactional
    @Modifying
    void deleteByDocumentId(Long documentId);
}
