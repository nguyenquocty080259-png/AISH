package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Object> {

    // 1. Đếm tổng số lượt thích của một tài liệu
    Long countByDocumentId(Long documentId);

    // 2. Kiểm tra User cụ thể đã thích tài liệu đó chưa dựa trên ID phức hợp
    boolean existsByUserIdAndDocumentId(Long userId, Long documentId);

    // 3. ĐÃ SỬA LỖI CHÍ MẠNG: Thêm @Transactional và @Modifying để hợp thức hóa lệnh xóa dữ liệu
    @Transactional
    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.userId = :userId AND f.documentId = :documentId")
    void deleteByUserIdAndDocumentId(@Param("userId") Long userId, @Param("documentId") Long documentId);

    // =========================================================================
    // THÊM SẴN CÁC HÀM TỐI ƯU (Dành cho việc mở rộng tính năng đồ án sau này)
    // =========================================================================

    // 4. Lấy danh sách ID các tài liệu mà một User cụ thể đã bấm Thả tim
    // (Rất hữu ích để làm trang "Tài liệu yêu thích của tôi" trên Frontend)
    @Query("SELECT f.documentId FROM Favorite f WHERE f.userId = :userId")
    List<Long> findDocumentIdsByUserId(@Param("userId") Long userId);

    // 5. Xóa toàn bộ lượt thích của một tài liệu khi tài liệu đó bị xóa cứng hoàn toàn khỏi hệ thống
    @Transactional
    @Modifying
    void deleteByDocumentId(Long documentId);
}