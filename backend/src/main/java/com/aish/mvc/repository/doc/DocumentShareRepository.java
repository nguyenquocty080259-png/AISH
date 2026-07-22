package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.DocumentShare;
import com.aish.mvc.entity.enums.ShareMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentShareRepository extends JpaRepository<DocumentShare, Long> {

    Optional<DocumentShare> findByDocumentIdAndSharedWithUserId(Long documentId, Long sharedWithUserId);

    boolean existsByDocumentIdAndSharedWithUserId(Long documentId, Long sharedWithUserId);

    // Có bất kỳ dòng link-share (ANYONE_WITH_LINK) nào cho tài liệu không.
    boolean existsByDocumentIdAndShareMode(Long documentId, ShareMode shareMode);

    List<DocumentShare> findByDocumentId(Long documentId);

    // Danh sách share cho 1 user cụ thể -> dựng "Được chia sẻ với tôi".
    List<DocumentShare> findBySharedWithUserId(Long sharedWithUserId);

    Optional<DocumentShare> findByShareToken(String shareToken);

    @Transactional
    @Modifying
    void deleteByDocumentIdAndSharedWithUserId(Long documentId, Long sharedWithUserId);

    @Transactional
    @Modifying
    void deleteByDocumentIdAndShareMode(Long documentId, ShareMode shareMode);

    // Dọn khi tài liệu bị xóa cứng.
    @Transactional
    @Modifying
    void deleteByDocumentId(Long documentId);
}
