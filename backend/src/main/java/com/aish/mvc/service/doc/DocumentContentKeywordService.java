package com.aish.mvc.service.doc;

import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.DocEmbedding;
import com.aish.mvc.entity.enums.ModerationKeywordType;
import com.aish.mvc.repository.doc.DocEmbeddingRepository;
import com.aish.mvc.service.ai.ToxicKeywordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * QUÉT TỪ KHOÁ CẤM TRONG NỘI DUNG TÀI LIỆU — lớp lọc RẺ chạy trước khi gọi AI kiểm duyệt, ở
 * luồng xin công khai tài liệu (xem DocumentServiceImpl.toggleVisibility).
 *
 * <p>Vì sao có lớp này: gọi AI tốn tiền và mất thời gian; nếu chỉ cần so từ khoá là đã phát hiện
 * vấn đề thì bỏ qua bước gọi AI.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentContentKeywordService {

    private final DocEmbeddingRepository docEmbeddingRepository;
    private final ToxicKeywordFilter toxicKeywordFilter;

    /**
     * Nội dung tài liệu có chứa từ khoá cấm không.
     *
     * <p>Đầu vào: tài liệu. Trả về: true nếu trúng từ khoá.
     *
     * <p>Các bước: (1) lấy chữ trong tài liệu từ các đoạn đã nạp cho AI (bảng doc_embeddings);
     * (2) tài liệu chưa nạp cho AI thì đành so tạm trên tiêu đề + mô tả; (3) đối chiếu với danh
     * sách từ khoá loại DOCUMENT_CONTENT.
     *
     * <p>Lỗi bất kỳ -> trả false ("cho qua") để không chặn oan; luồng vẫn còn AI và Admin duyệt phía sau.
     */
    public boolean matches(DocDocument document) {
        try {
            List<DocEmbedding> chunks = docEmbeddingRepository
                    .findByDocument_IdOrderByChunkIndexAsc(document.getId());
            String content = chunks.isEmpty()
                    ? fallbackContent(document)
                    : chunks.stream()
                            .map(DocEmbedding::getChunkText)
                            .filter(text -> text != null && !text.isBlank())
                            .collect(Collectors.joining("\n"));
            return toxicKeywordFilter.matches(content, ModerationKeywordType.DOCUMENT_CONTENT);
        } catch (Exception exception) {
            log.warn("Không thể kiểm tra từ khóa nội dung cho tài liệu {}; tiếp tục AI screen theo fail-open.",
                    document.getId(), exception);
            return false;
        }
    }

    // Tài liệu chưa được nạp cho AI (chưa có đoạn nào trong doc_embeddings) thì không đọc được
    // nội dung file — đành quét tạm trên tiêu đề và mô tả, có còn hơn không.
    private String fallbackContent(DocDocument document) {
        String title = document.getTitle() == null ? "" : document.getTitle();
        String description = document.getDescription() == null ? "" : document.getDescription();
        return title + "\n" + description;
    }
}
