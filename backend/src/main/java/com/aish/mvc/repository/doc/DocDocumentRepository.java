package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.IngestStatus;
import com.aish.mvc.entity.enums.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Tài liệu KHÔNG bị xoá của 1 user — dùng để giới hạn phạm vi truy xuất RAG vào
    // đúng tài liệu của chính user đó (DEC-011), không rò rỉ private của người khác.
    List<DocDocument> findByDeletedAtIsNullAndUser_Id(Long userId);

    // Dùng bởi DbSeedRunner để dọn TOÀN BỘ document (kể cả đã ở thùng rác) của các user seed —
    // không lọc deletedAt vì mục đích là xoá sạch, không phải hiển thị.
    List<DocDocument> findByUser_IdIn(List<Long> userIds);

    // Trang "Yêu thích": lấy đúng các document theo id đã favorite, loại tài liệu đã vào
    // thùng rác (deletedAt != null) để không hiện lẫn như một favorite bình thường.
    List<DocDocument> findByIdInAndDeletedAtIsNull(List<Long> ids);

    // Cho GET /api/admin/stats — đếm rẻ, không load entity.
    long countByDeletedAtIsNull();
    long countByVisibilityAndDeletedAtIsNull(DocumentVisibility visibility);
    long countByIngestStatusAndDeletedAtIsNull(IngestStatus ingestStatus);
    long countByIngestStatusIsNullAndDeletedAtIsNull();

    // DEC-030: chặn xóa subject nếu còn BẤT KỲ document nào tham chiếu (kể cả đã ở thùng rác) —
    // không lọc deletedAt vì document_subjects vẫn còn row cho tới khi xóa vĩnh viễn, xóa subject
    // lúc đó sẽ vi phạm FK. Không cần phân biệt "còn 1 subject" hay nhiều: attach = chặn, luôn an toàn.
    boolean existsBySubjects_Id(Long subjectId);

    Page<DocDocument> findByVisibility(
            DocumentVisibility visibility,
            Pageable pageable
    );
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
            "AND (:subjectId IS NULL OR EXISTS (SELECT 1 FROM d.subjects s WHERE s.id = :subjectId)) " +
            "AND (:tagId IS NULL OR EXISTS (SELECT 1 FROM d.tags t WHERE t.id = :tagId))")
    List<DocDocument> findCommunityDocuments(@Param("pub") DocumentVisibility pub,
                                             @Param("keyword") String keyword,
                                             @Param("subjectId") Long subjectId,
                                             @Param("tagId") Long tagId);
    // Dùng bởi BulkIngestRunner: lấy 1 lô tài liệu CHƯA ingest, thứ tự id tăng dần để lần
    // chạy sau (resume sau khi bị rate-limit dừng giữa chừng) luôn nhặt tiếp đúng thứ tự,
    // không bỏ sót/không lặp lại tài liệu đã xử lý. JOIN FETCH d.files vì BulkIngestRunner đọc
    // doc.getFiles() để log định dạng SAU KHI phiên gốc của query này đã đóng (không transactional).
    @Query("SELECT DISTINCT d FROM DocDocument d LEFT JOIN FETCH d.files " +
            "WHERE d.ingestStatus = :status ORDER BY d.id ASC")
    List<DocDocument> findNotIngestedBatch(@Param("status") IngestStatus status, Pageable pageable);

    // Candidate pool cho recommendations: PUBLIC + đã qua kiểm duyệt AI + chưa xoá.
    // Về mặt cấu trúc PUBLIC luôn kéo theo APPROVED (toggleVisibility chỉ set PUBLIC khi
    // PASS/Admin-approve), nhưng lọc rõ ràng ở đây để không phụ thuộc ngầm vào invariant đó.
    @Query("SELECT DISTINCT d FROM DocDocument d LEFT JOIN FETCH d.subjects " +
            "WHERE d.deletedAt IS NULL AND d.visibility = :pub AND d.moderationStatus = :approved")
    List<DocDocument> findPublicApprovedDocuments(@Param("pub") DocumentVisibility pub,
                                                   @Param("approved") ModerationStatus approved);
}
