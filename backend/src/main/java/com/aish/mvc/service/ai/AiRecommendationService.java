package com.aish.mvc.service.ai;

import com.aish.mvc.dto.ai.RecommendedDocumentDTO;

import java.util.List;

public interface AiRecommendationService {

    // Tab "Liên quan": tài liệu PUBLIC khác cùng chủ đề (subject) với 1 tài liệu cho trước,
    // trending làm tie-break. currentUserId null = guest (vẫn được, vì kết quả luôn PUBLIC).
    List<RecommendedDocumentDTO> recommendRelatedToDocument(Long documentId, Long currentUserId, int limit);

    // "Dành cho bạn": dựa trên tín hiệu cá nhân (tài liệu của mình + favorite + recently-viewed
    // + collections) -> tập subject user quan tâm; fallback trending thuần nếu chưa có tín hiệu.
    List<RecommendedDocumentDTO> recommendForUser(Long currentUserId, int limit);
}
