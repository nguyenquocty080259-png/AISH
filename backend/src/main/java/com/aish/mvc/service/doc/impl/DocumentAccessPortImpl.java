package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.service.doc.DocumentAccessPort;
import com.aish.mvc.service.doc.DocumentService;
import com.aish.mvc.service.doc.DocumentShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * NƠI TRẢ LỜI CÂU HỎI "người này có được xem tài liệu kia không" — một chỗ duy nhất giữ luật
 * quyền, để các module khác (Bộ sưu tập, Lịch sử xem, Tương tác, Gợi ý, Chat AI) dùng chung thay
 * vì mỗi nơi tự viết lại một kiểu (dễ sai lệch, dễ hở bảo mật).
 *
 * <p>Khác với DocumentService: ở đây trả về true/false hoặc Optional rỗng chứ KHÔNG ném lỗi, vì
 * bên gọi chỉ muốn biết "có được xem không" để lọc danh sách, không phải để chặn request.
 */
@Service
public class DocumentAccessPortImpl implements DocumentAccessPort {

    @Autowired private DocDocumentRepository docDocumentRepository;
    @Autowired private DocumentService documentService;
    @Autowired private DocumentShareService documentShareService;

    /**
     * KIỂM TRA QUYỀN XEM tài liệu. Đầu vào: id tài liệu + id người xem. Trả về: true nếu được xem.
     *
     * <p>Xét lần lượt 5 trường hợp bên dưới. Tài liệu không tồn tại cũng trả false (không phân
     * biệt "không có" với "không được xem", để không lộ ra là tài liệu đó tồn tại).
     */
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

    // Truy vấn bảng share thật: khả dụng nếu user được mời trực tiếp (RESTRICTED) hoặc tài liệu
    // đang bật link-share (ANYONE_WITH_LINK). Người được share chỉ XEM/tải/hỏi AI/bình luận —
    // sửa/xóa/đổi visibility vẫn bị chặn ở tầng service tài liệu (yêu cầu đúng chủ sở hữu).
    private boolean isSharedTo(Long documentId, Long currentUserId) {
        return documentShareService.hasShareAccess(documentId, currentUserId);
    }

    /**
     * Lấy dữ liệu đầy đủ của tài liệu NẾU người này được xem. Đầu vào: id tài liệu + id người xem.
     * Trả về: Optional rỗng khi không được xem, thay vì báo lỗi.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<DocumentResponseDTO> getDocumentDto(Long documentId, Long currentUserId) {
        if (!isAvailableTo(documentId, currentUserId)) return Optional.empty();
        // Tái dùng đúng DTO mà GET /api/documents/{id} trả ra.
        return Optional.of(documentService.getDocumentById(documentId));
    }

    /**
     * Chỉ lấy TIÊU ĐỀ tài liệu (nếu được xem) — dùng cho chỗ chỉ cần hiển thị tên, khỏi phải
     * dựng cả bộ dữ liệu nặng gồm bình luận và số liệu tương tác.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<String> getDocumentTitle(Long documentId, Long currentUserId) {
        if (!isAvailableTo(documentId, currentUserId)) return Optional.empty();
        return docDocumentRepository.findById(documentId).map(DocDocument::getTitle);
    }

    /** Có được thêm tài liệu này vào bộ sưu tập không — dùng đúng luật quyền như xem tài liệu. */
    @Override
    @Transactional(readOnly = true)
    public boolean canAddToCollection(Long documentId, Long currentUserId) {
        // Chỉ cho add doc user ĐANG có quyền xem lúc add (của mình / PUBLIC / SHARED-cho-mình).
        // Trùng khớp với luật availability -> tái dùng.
        return isAvailableTo(documentId, currentUserId);
    }
}
