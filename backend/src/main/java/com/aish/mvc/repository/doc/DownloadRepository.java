package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.Download;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DownloadRepository extends JpaRepository<Download, Long> {

    Long countByDocumentId(Long documentId);

    @Transactional
    @Modifying
    void deleteByDocumentId(Long documentId);
}