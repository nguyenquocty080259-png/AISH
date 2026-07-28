package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.DocumentResponseDTO;

import java.util.Optional;

/**
 * Cổng hỏi quyền xem tài liệu cho các module KHÁC (Collections, AI chat, tương tác trong chính
 * module tài liệu). Tách riêng khỏi {@link DocumentService} để luật availability nằm một chỗ,
 * và để bên gọi không phải bắt exception chỉ để biết "có được xem không" — cổng này trả về
 * boolean/Optional thay vì ném lỗi.
 *
 * <p>Luật availability, xét theo thứ tự: tài liệu đã xoá mềm -> không khả dụng với bất kỳ ai;
 * tài liệu của chính mình -> luôn khả dụng kể cả PRIVATE; PUBLIC -> khả dụng; SHARED -> khả
 * dụng nếu đã được chia sẻ cho user này; còn lại (PRIVATE của người khác) -> không khả dụng.
 */
public interface DocumentAccessPort {

    /**
     * User hiện tại có được XEM tài liệu này không, theo luật availability ở trên.
     *
     * @param currentUserId null được coi như chưa xác định danh tính (không phải chủ sở hữu,
     *                      cũng không được tính là người được chia sẻ)
     * @return false khi tài liệu không tồn tại — bên gọi không phân biệt "không có" với "không
     *         được xem", đúng chủ ý để không lộ sự tồn tại của tài liệu riêng tư
     */
    boolean isAvailableTo(Long documentId, Long currentUserId);

    /**
     * DTO tài liệu đầy đủ, dùng lại đúng DTO mà {@code GET /api/documents/{id}} trả về.
     *
     * @return {@link Optional#empty()} nếu tài liệu không khả dụng với user này
     */
    Optional<DocumentResponseDTO> getDocumentDto(Long documentId, Long currentUserId);

    /** Chỉ tiêu đề tài liệu — cho chỗ hiển thị nhãn/tham chiếu, tránh dựng cả DTO. */
    Optional<String> getDocumentTitle(Long documentId, Long currentUserId);

    /**
     * Có được phép thêm tài liệu này vào collection lúc này không. Cùng luật với
     * {@link #isAvailableTo}: chỉ thêm được tài liệu đang thực sự xem được.
     */
    boolean canAddToCollection(Long documentId, Long currentUserId);
}
