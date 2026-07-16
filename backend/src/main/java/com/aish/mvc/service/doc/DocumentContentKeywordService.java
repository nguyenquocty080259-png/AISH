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

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentContentKeywordService {

    private final DocEmbeddingRepository docEmbeddingRepository;
    private final ToxicKeywordFilter toxicKeywordFilter;

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

    private String fallbackContent(DocDocument document) {
        String title = document.getTitle() == null ? "" : document.getTitle();
        String description = document.getDescription() == null ? "" : document.getDescription();
        return title + "\n" + description;
    }
}
