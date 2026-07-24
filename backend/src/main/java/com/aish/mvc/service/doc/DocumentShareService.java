package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.DocumentShareRecipientDTO;
import com.aish.mvc.dto.doc.ShareRequestDTO;
import com.aish.mvc.dto.doc.ShareResponseDTO;
import com.aish.mvc.dto.doc.SharedWithMeItemDTO;

import java.util.List;

public interface DocumentShareService {

    // Chỉ Author/Owner được gọi. RESTRICTED -> mời từng user + notify; ANYONE_WITH_LINK -> sinh token;
    // NONE -> tắt link-share. Đặt visibility = SHARED khi có người/link được cấp quyền.
    ShareResponseDTO shareDocument(Long documentId, ShareRequestDTO request);

    // Chỉ Author/Owner: gỡ quyền của 1 user cụ thể.
    void revokeShare(Long documentId, Long userId);

    // Danh sách tài liệu được chia sẻ với người dùng hiện tại (còn khả dụng).
    List<SharedWithMeItemDTO> listSharedWithMe();

    // CHỈ Author/Owner: danh sách người đang được chia sẻ tài liệu (RESTRICTED) + quyền của họ.
    List<DocumentShareRecipientDTO> listShareRecipients(Long documentId);

    // Dùng bởi DocumentAccessPort: user có được xem tài liệu qua share không.
    boolean hasShareAccess(Long documentId, Long userId);
}
