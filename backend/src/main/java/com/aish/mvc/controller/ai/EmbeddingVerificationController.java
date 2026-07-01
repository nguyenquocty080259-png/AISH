package com.aish.mvc.controller.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

/**
 * Temporary verification hook for task 2A (embedding + vector store foundation).
 * On-demand only — nothing here runs at application startup, so a fake/missing
 * GEMINI_API_KEY cannot break boot. Remove once the real ingestion pipeline
 * (chunking/retrieval) lands and exercises the embedding model directly.
 */
@RestController
@RequestMapping("/api/ai/debug")
@RequiredArgsConstructor
public class EmbeddingVerificationController {

    private final EmbeddingModel embeddingModel;

    @GetMapping("/embedding-check")
    public ResponseEntity<Map<String, Object>> checkEmbedding() {
        float[] vector = embeddingModel.embed("AISH embedding connectivity check");
        return ResponseEntity.ok(Map.of(
                "dimension", vector.length,
                "sample", Arrays.copyOf(vector, Math.min(5, vector.length))
        ));
    }
}
