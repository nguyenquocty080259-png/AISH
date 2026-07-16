package com.aish.mvc.service.ai;

import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.DocEmbedding;
import com.aish.mvc.repository.doc.DocEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Shared, bounded document sample used by moderation and metadata assistance. */
@Service
@RequiredArgsConstructor
public class AiContentSignalService {
    public static final int MAX_SAMPLE_CHARS = 1500;
    private static final int MAX_SAMPLE_CHUNKS = 5;
    private final DocEmbeddingRepository docEmbeddingRepository;

    public String buildContentSignal(DocDocument doc) {
        List<DocEmbedding> chunks = docEmbeddingRepository.findByDocument_IdOrderByChunkIndexAsc(doc.getId());
        StringBuilder sample = new StringBuilder();
        if (!chunks.isEmpty()) {
            for (int i = 0; i < chunks.size() && i < MAX_SAMPLE_CHUNKS && sample.length() < MAX_SAMPLE_CHARS; i++) {
                if (chunks.get(i).getChunkText() != null) sample.append(chunks.get(i).getChunkText()).append("\n\n");
            }
        } else {
            if (doc.getTitle() != null) sample.append(doc.getTitle()).append("\n");
            if (doc.getDescription() != null) sample.append(doc.getDescription());
        }
        String result = sample.toString().strip();
        return result.length() > MAX_SAMPLE_CHARS ? result.substring(0, MAX_SAMPLE_CHARS) : result;
    }
}
