package com.aish.mvc.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// GET /api/ai/recommendations - danh sách gợi ý đã xếp hạng. score lộ ra để giải thích
// vì sao 1 tài liệu được gợi ý (yêu cầu "explainable ranking", không phải black-box).
@Getter
@AllArgsConstructor
public class RecommendedDocumentDTO {
    private Long documentId;
    private String title;
    private String ownerName;
    private List<String> subjectNames;
    private Long favoriteCount;
    private Long downloadCount;
    private Double averageRating;
    private double score;
}
