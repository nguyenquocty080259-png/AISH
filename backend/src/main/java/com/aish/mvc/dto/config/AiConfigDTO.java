package com.aish.mvc.dto.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// topK/similarityThreshold/recentMessageLimit: dùng cho AiChatService (truy vấn RAG + ngữ cảnh chat).
// các weight: dùng cho RecommendationServiceImpl (chấm điểm gợi ý tài liệu).
// chunkSize: dùng cho DocEmbeddingServiceImpl (cắt tài liệu để embed).
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiConfigDTO {
    private Integer topK;
    private Double similarityThreshold;
    private Integer recentMessageLimit;
    private Double subjectOverlapWeight;
    private Double favoriteWeight;
    private Double downloadWeight;
    private Double ratingWeight;
    private Integer chunkSize;
}
