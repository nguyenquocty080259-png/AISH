package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.service.doc.DocumentAccessPort;
import com.aish.mvc.service.doc.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DocumentAccessPortImpl implements DocumentAccessPort {

    @Autowired private DocDocumentRepository docDocumentRepository;
    @Autowired private DocumentService documentService;

    @Override
    @Transactional(readOnly = true)
    public boolean isAvailableTo(Long documentId, Long currentUserId) {
        DocDocument doc = docDocumentRepository.findById(documentId).orElse(null);
        if (doc == null) return false;

        // 1. Trash: đã xóa mềm -> không khả dụng cho bất kỳ ai.
        if (doc.getDeletedAt() != null) return false;

        // 2. Của chính mình -> luôn khả dụng (kể cả PRIVATE).
        if (currentUserId != null && doc.getUser().getId().equals(currentUserId)) return true;

        // 3. PUBLIC -> khả dụng.
        // repo CHƯA có moderation. Tạm coi mọi PUBLIC khả dụng. TODO(Person4): thêm điều kiện duyệt.
        if (doc.getVisibility() == DocumentVisibility.PUBLIC) return true;

        // 4. SHARED -> chỉ khả dụng nếu đã share cho user này.
        if (doc.getVisibility() == DocumentVisibility.SHARED) return isSharedTo(documentId, currentUserId);

        // 5. Còn lại (PRIVATE của người khác) -> không khả dụng.
        return false;
    }

    // ⚠️ STUB — repo CHƯA có bảng share nên LUÔN trả false cho người không phải owner.
    // TODO(Person3-Prompt4): điền logic thật khi có bảng share.
    private boolean isSharedTo(Long documentId, Long currentUserId) {
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DocumentResponseDTO> getDocumentDto(Long documentId, Long currentUserId) {
        if (!isAvailableTo(documentId, currentUserId)) return Optional.empty();
        // Tái dùng đúng DTO mà GET /api/documents/{id} trả ra.
        return Optional.of(documentService.getDocumentById(documentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> getDocumentTitle(Long documentId, Long currentUserId) {
        if (!isAvailableTo(documentId, currentUserId)) return Optional.empty();
        return docDocumentRepository.findById(documentId).map(DocDocument::getTitle);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAddToCollection(Long documentId, Long currentUserId) {
        // Chỉ cho add doc user ĐANG có quyền xem lúc add (của mình / PUBLIC / SHARED-cho-mình).
        // Trùng khớp với luật availability -> tái dùng.
        return isAvailableTo(documentId, currentUserId);
    }
}
