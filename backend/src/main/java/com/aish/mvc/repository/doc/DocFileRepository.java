package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.DocFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocFileRepository extends JpaRepository<DocFile, Long> {
    // Hàm tìm tất cả các file của một tài liệu cụ thể
    List<DocFile> findByDocumentId(Long documentId);

    // Idempotency key cho classpath document seeder: fileUrl local chính là stored filename.
    boolean existsByFileUrl(String fileUrl);

    // Tổng dung lượng đã dùng của 1 user, tách theo nơi lưu - dùng cho quota (xem
    // DocumentServiceImpl.enforceUploadQuota). CHỦ Ý không lọc document.deletedAt: tài liệu
    // trong thùng rác vẫn chiếm disk/Cloudinary nên vẫn tính vào quota cho tới khi xoá vĩnh viễn.
    // COALESCE về 0 vì SUM trả NULL khi user chưa có file nào.
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM DocFile f " +
            "WHERE f.document.user.id = :userId AND LOWER(f.resourceType) = 'local'")
    long sumLocalFileSizeByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM DocFile f " +
            "WHERE f.document.user.id = :userId AND LOWER(f.resourceType) <> 'local'")
    long sumCloudFileSizeByUserId(@Param("userId") Long userId);

    // Tổng dung lượng đã dùng TOÀN HỆ THỐNG, tách theo nơi lưu — cho dashboard admin.
    // Mirror sumLocal/CloudFileSizeByUserId nhưng không lọc user. Vẫn tính cả tài liệu trong
    // thùng rác (giống quy ước quota). COALESCE về 0 khi chưa có file.
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM DocFile f " +
            "WHERE LOWER(f.resourceType) = 'local'")
    long sumLocalFileSizeAll();

    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM DocFile f " +
            "WHERE LOWER(f.resourceType) <> 'local'")
    long sumCloudFileSizeAll();
}
