package com.aish.mvc.service.doc;

import com.aish.mvc.dto.ai.IngestResponseDTO;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * Nạp tài liệu cho AI đọc và tra cứu lại (nền tảng của tính năng "Hỏi AI về tài liệu này").
 *
 * <p>Ý tưởng: cắt nội dung tài liệu thành nhiều đoạn nhỏ, biến mỗi đoạn thành một dãy số thể
 * hiện ý nghĩa (vector) rồi lưu lại; khi có câu hỏi thì tìm những đoạn có ý nghĩa gần câu hỏi
 * nhất để AI trả lời kèm trích dẫn.
 */
public interface DocEmbeddingService {

    // Đọc PDF của documentId, chunk, embed (Gemini), lưu doc_embeddings + nạp vào SimpleVectorStore.
    // Chỉ owner/admin được ingest. Không phải PDF -> skip rõ ràng, không throw.
    IngestResponseDTO ingest(Long documentId);

    // File này AI có đọc được không (false với ảnh/video/nhạc/file nén/file chạy) — FE dùng để
    // ẩn/hiện nút "Hỏi AI" ngay ở danh sách tài liệu.
    boolean isAiSupported(String fileName, String fileType);

    // Nạp lại toàn bộ vector đã embed từ doc_embeddings vào SimpleVectorStore lúc khởi động —
    // KHÔNG gọi lại EmbeddingModel (đọc thẳng vector JSON đã lưu).
    void hydrateFromDatabase();

    // top-K similarity search giới hạn trong danh sách documentId cho trước (DEC-011 access scoping).
    List<Document> retrieveChunks(String query, List<Long> documentIds, int topK, double similarityThreshold);
}
