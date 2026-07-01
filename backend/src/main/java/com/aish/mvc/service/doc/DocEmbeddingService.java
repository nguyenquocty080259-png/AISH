package com.aish.mvc.service.doc;

import com.aish.mvc.dto.ai.IngestResponseDTO;
import org.springframework.ai.document.Document;

import java.util.List;

public interface DocEmbeddingService {

    // Đọc PDF của documentId, chunk, embed (Gemini), lưu doc_embeddings + nạp vào SimpleVectorStore.
    // Chỉ owner/admin được ingest. Không phải PDF -> skip rõ ràng, không throw.
    IngestResponseDTO ingest(Long documentId);

    // Nạp lại toàn bộ vector đã embed từ doc_embeddings vào SimpleVectorStore lúc khởi động —
    // KHÔNG gọi lại EmbeddingModel (đọc thẳng vector JSON đã lưu).
    void hydrateFromDatabase();

    // top-K similarity search giới hạn trong danh sách documentId cho trước (DEC-011 access scoping).
    List<Document> retrieveChunks(String query, List<Long> documentIds, int topK, double similarityThreshold);
}
