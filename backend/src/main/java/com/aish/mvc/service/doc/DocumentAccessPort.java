package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.DocumentResponseDTO;

import java.util.Optional;

// Cổng truy cập tài liệu cho module Collections — tách khỏi DocumentService để cô lập
// luật availability/permission. Trả về DTO document sẵn có (KHÔNG tạo DTO document mới).
public interface DocumentAccessPort {

    // Người dùng hiện tại có được XEM tài liệu này không (theo luật availability 5 bước).
    boolean isAvailableTo(Long documentId, Long currentUserId);

    // DTO document đầy đủ (tái dùng của GET /api/documents/{id}) — chỉ trả khi khả dụng.
    Optional<DocumentResponseDTO> getDocumentDto(Long documentId, Long currentUserId);

    Optional<String> getDocumentTitle(Long documentId, Long currentUserId);

    // Có được phép ADD tài liệu này vào collection lúc này không.
    boolean canAddToCollection(Long documentId, Long currentUserId);
}
