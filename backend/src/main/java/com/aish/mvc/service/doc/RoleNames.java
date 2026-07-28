package com.aish.mvc.service.doc;

/**
 * Tên vai trò mà module tài liệu cần so khớp. Giá trị gốc do module Authentication sở hữu
 * (bảng auth_roles, SecurityConfig); ở đây chỉ là điểm tham chiếu DUY NHẤT cho phần tài liệu,
 * để bốn chỗ kiểm tra quyền admin trong module không mỗi nơi gõ lại một chuỗi.
 *
 * <p>Chưa gom được cho toàn repo vì các module khác (admin, AI, report, notification) cũng đang
 * dùng literal riêng, và sửa chúng nằm ngoài phạm vi module tài liệu.
 */
public final class RoleNames {

    /** Vai trò quản trị: được miễn một số chốt chặn và là người nhận thông báo kiểm duyệt. */
    public static final String ADMIN = "ADMIN";

    private RoleNames() {
    }
}
