package com.aish.mvc.config;

import com.aish.mvc.service.doc.DocEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// Nạp lại toàn bộ vector đã embed từ doc_embeddings vào SimpleVectorStore lúc khởi động,
// để restart không mất dữ liệu đã ingest và không tốn quota Gemini để embed lại.
@Component
@RequiredArgsConstructor
public class VectorStoreHydrationRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(VectorStoreHydrationRunner.class);

    private final DocEmbeddingService docEmbeddingService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            docEmbeddingService.hydrateFromDatabase();
        }
        catch (Exception e) {
            logger.warn("Không thể hydrate SimpleVectorStore từ doc_embeddings lúc khởi động: {}", e.getMessage());
        }
    }
}
