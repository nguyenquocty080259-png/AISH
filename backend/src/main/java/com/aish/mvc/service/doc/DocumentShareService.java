package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.DocumentShareRecipientDTO;
import com.aish.mvc.dto.doc.ShareRequestDTO;
import com.aish.mvc.dto.doc.ShareResponseDTO;
import com.aish.mvc.dto.doc.SharedWithMeItemDTO;

import java.util.List;

/**
 * Chia sẻ tài liệu cho người dùng khác.
 *
 * <p>Chia sẻ KHÔNG đổi chế độ hiển thị của tài liệu: chỉ tài liệu đang PUBLIC mới chia sẻ được,
 * và việc chia sẻ chỉ cấp thêm đặc quyền (hiện ở trang "Được chia sẻ với tôi", quyền
 * {@link com.aish.mvc.entity.enums.SharePermission}, thông báo cho người nhận). Gỡ chia sẻ chỉ
 * rút đặc quyền đó — người bị gỡ vẫn xem được vì tài liệu vốn công khai.
 *
 * <p>Mọi thao tác quản lý (chia sẻ, gỡ, xem danh sách người nhận) đều CHỈ dành cho chủ sở hữu;
 * người được chia sẻ chỉ đọc. Tài liệu trong thùng rác không chia sẻ được.
 */
public interface DocumentShareService {

    /**
     * Chia sẻ tài liệu theo chế độ trong request — chỉ chủ sở hữu.
     *
     * <p>RESTRICTED: mời theo email (ưu tiên) hoặc userIds, có thông báo cho người nhận; mời
     * lại người đã có quyền thì CẬP NHẬT bản ghi cũ chứ không tạo dòng trùng.
     * ANYONE_WITH_LINK: sinh (hoặc dùng lại) token link-share.
     * NONE: tắt link-share — là thao tác thu hồi nên không bị gác điều kiện PUBLIC.
     *
     * @throws com.aish.mvc.exception.ResourceNotFoundException tài liệu không tồn tại/đã xoá
     *         mềm, hoặc email người nhận chưa có tài khoản
     * @throws com.aish.mvc.exception.ForbiddenException người gọi không phải chủ sở hữu
     * @throws IllegalArgumentException thiếu mode, quyền EDITOR (chưa hỗ trợ ở V1), tài liệu
     *         chưa công khai, tự chia sẻ cho chính mình, hoặc người nhận đã bị khoá
     * @throws IllegalStateException email khớp nhiều tài khoản — từ chối thay vì đoán
     */
    ShareResponseDTO shareDocument(Long documentId, ShareRequestDTO request);

    /** Gỡ quyền chia sẻ của một user cụ thể — chỉ chủ sở hữu. Gỡ id chưa từng được chia sẻ là no-op. */
    void revokeShare(Long documentId, Long userId);

    /** Tài liệu đang được chia sẻ với user hiện tại; tài liệu đã xoá mềm bị ẩn khỏi danh sách. */
    List<SharedWithMeItemDTO> listSharedWithMe();

    /**
     * Người đang được chia sẻ tài liệu này kèm quyền của họ — CHỈ chủ sở hữu gọi được
     * (người được chia sẻ không thấy ai khác). Bản ghi link-share không gắn với người cụ thể
     * nên không xuất hiện trong danh sách.
     */
    List<DocumentShareRecipientDTO> listShareRecipients(Long documentId);

    /**
     * User có xem được tài liệu qua đường chia sẻ không — chốt quyền dùng chung của mọi lối đọc
     * nội dung (chi tiết, xem trước, tải, {@link DocumentAccessPort}).
     *
     * @return true nếu user được mời trực tiếp, hoặc tài liệu đang bật link-share; false khi
     *         userId là null
     */
    boolean hasShareAccess(Long documentId, Long userId);
}
