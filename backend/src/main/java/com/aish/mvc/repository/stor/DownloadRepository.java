package com.aish.mvc.repository.stor;

import com.aish.mvc.entity.doc.Download;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DownloadRepository extends JpaRepository<Download, Long> {
    // Đếm tổng số lượt tải của một tài liệu
    Long countByDocumentId(Long documentId);
}