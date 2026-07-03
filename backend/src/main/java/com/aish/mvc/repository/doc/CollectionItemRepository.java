package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.CollectionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionItemRepository extends JpaRepository<CollectionItem, Long> {

    List<CollectionItem> findByCollection_IdOrderByAddedAtAsc(Long collectionId);

    boolean existsByCollection_IdAndDocumentId(Long collectionId, Long documentId);

    Optional<CollectionItem> findByCollection_IdAndDocumentId(Long collectionId, Long documentId);

    long countByCollection_Id(Long collectionId);

    // Dùng bởi DbSeedRunner khi dọn seed cũ — 1 document seed có thể đã được người dùng thật
    // thêm vào collection của họ; phải dọn tham chiếu này trước khi xoá document.
    @Transactional
    @Modifying
    void deleteByDocumentId(Long documentId);
}
