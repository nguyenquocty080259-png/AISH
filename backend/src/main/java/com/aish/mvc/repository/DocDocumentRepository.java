package com.aish.mvc.repository;

import com.aish.mvc.entity.DocDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocDocumentRepository extends JpaRepository<DocDocument, Long> {
    // Sau này bạn có thể thêm các hàm tìm kiếm theo Subject tại đây
}