package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.RecommendedDocumentDTO;

import java.util.List;

/**
 * Gợi ý tài liệu. Cách tính đơn giản, KHÔNG dùng AI: chấm điểm theo số môn học trùng nhau (trọng
 * số lớn nhất) cộng thêm lượt thích / lượt tải / điểm sao để phân định khi bằng điểm.
 *
 * <p>Ứng viên luôn chỉ gồm tài liệu PUBLIC đã được Admin duyệt, và còn được kiểm tra lại quyền
 * xem một lần nữa trước khi trả về.
 */
public interface RecommendationService {

    // Tab "Liên quan": tài liệu PUBLIC khác cùng chủ đề (subject) với 1 tài liệu cho trước,
    // trending làm tie-break. currentUserId null = guest (vẫn được, vì kết quả luôn PUBLIC).
    List<RecommendedDocumentDTO> recommendRelatedToDocument(Long documentId, Long currentUserId, int limit);

    // "Dành cho bạn": dựa trên tín hiệu cá nhân (tài liệu của mình + favorite + recently-viewed
    // + collections) -> tập subject user quan tâm; fallback trending thuần nếu chưa có tín hiệu.
    List<RecommendedDocumentDTO> recommendForUser(Long currentUserId, int limit);
}
