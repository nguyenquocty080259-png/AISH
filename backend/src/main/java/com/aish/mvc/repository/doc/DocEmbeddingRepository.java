package com.aish.mvc.repository.doc;

import com.aish.mvc.entity.doc.DocEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocEmbeddingRepository extends JpaRepository<DocEmbedding, Long> {

    List<DocEmbedding> findByDocument_IdOrderByChunkIndexAsc(Long documentId);

    void deleteByDocument_Id(Long documentId);

    // JOIN FETCH document + user để tránh N+1 khi hydrate toàn bộ vector store lúc khởi động
    // (user cần cho citation "author" — xem DocEmbeddingServiceImpl.authorOf).
    @Query("SELECT e FROM DocEmbedding e JOIN FETCH e.document d JOIN FETCH d.user")
    List<DocEmbedding> findAllWithDocument();
}
