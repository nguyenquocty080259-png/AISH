package com.aish.mvc.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStoreContent;

import java.util.List;

/**
 * SimpleVectorStore.add() always calls the EmbeddingModel to compute the vector —
 * there is no public API to inject an already-computed embedding. hydrate() bypasses
 * that (writing straight into the protected in-memory store this class inherits) so
 * restoring previously-embedded chunks from doc_embeddings on startup, or right after
 * ingest-time embedding, never re-calls Gemini for the same text twice.
 */
public class HydratableSimpleVectorStore extends SimpleVectorStore {

    protected HydratableSimpleVectorStore(SimpleVectorStore.SimpleVectorStoreBuilder builder) {
        super(builder);
    }

    public static HydratableSimpleVectorStore create(EmbeddingModel embeddingModel) {
        return new HydratableSimpleVectorStore(SimpleVectorStore.builder(embeddingModel));
    }

    public void hydrate(List<SimpleVectorStoreContent> contents) {
        for (SimpleVectorStoreContent content : contents) {
            this.store.put(content.getId(), content);
        }
    }
}
