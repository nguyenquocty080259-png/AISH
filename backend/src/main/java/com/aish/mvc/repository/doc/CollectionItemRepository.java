package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.CollectionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionItemRepository extends JpaRepository<CollectionItem, Long> {

    List<CollectionItem> findByCollection_IdOrderByAddedAtAsc(Long collectionId);

    boolean existsByCollection_IdAndDocumentId(Long collectionId, Long documentId);

    Optional<CollectionItem> findByCollection_IdAndDocumentId(Long collectionId, Long documentId);

    long countByCollection_Id(Long collectionId);
}
