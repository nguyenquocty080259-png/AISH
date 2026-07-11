package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.DocFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocFileRepository extends JpaRepository<DocFile, Long> {
    // Hàm tìm tất cả các file của một tài liệu cụ thể
    List<DocFile> findByDocumentId(Long documentId);

    // Idempotency key cho classpath document seeder: fileUrl local chính là stored filename.
    boolean existsByFileUrl(String fileUrl);
}
