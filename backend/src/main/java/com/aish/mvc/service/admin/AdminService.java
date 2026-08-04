package com.aish.mvc.service.admin;

import com.aish.mvc.dto.auth.admin.AdminCreateUserRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminResetPasswordRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminUpdateUserRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminUpdateUserStatusRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminUserResponseDTO;
import com.aish.mvc.dto.doc.AdminAppealResponseDTO;
import com.aish.mvc.dto.doc.AdminStatsDTO;
import com.aish.mvc.dto.doc.AdminCommentReviewDTO;
import com.aish.mvc.entity.enums.AppealStatus;
import com.aish.mvc.entity.enums.CommentStatus;

import java.util.List;

/**
 * Nghiệp vụ dành riêng cho trang QUẢN TRỊ (Admin): xử lý kháng cáo kiểm duyệt, duyệt/từ chối bình
 * luận bị AI gắn cờ, xác nhận/gỡ xác nhận thủ công cho tài liệu, số liệu thống kê hệ thống, và
 * quản lý người dùng (tạo, sửa, đổi trạng thái, reset mật khẩu). Toàn bộ chỉ Admin gọi được —
 * chốt quyền ở SecurityConfig (/api/admin/** yêu cầu ROLE_ADMIN).
 */
public interface AdminService {

    // status == null -> mọi appeal (mọi trạng thái).
    List<AdminAppealResponseDTO> listAppeals(AppealStatus status);

    // Chỉ xử lý được appeal đang APPEAL_PENDING. Approve: appeal -> APPEAL_APPROVED,
    // document -> PUBLIC/APPROVED.
    AdminAppealResponseDTO approveAppeal(Long appealId, String adminNote);

    // Reject: appeal -> APPEAL_REJECTED, document giữ nguyên PRIVATE/REJECTED.
    AdminAppealResponseDTO rejectAppeal(Long appealId, String adminNote);

    // Danh sách bình luận theo trạng thái kiểm duyệt (vd PENDING_REVIEW chờ Admin xử lý).
    List<AdminCommentReviewDTO> listComments(CommentStatus status);

    // Duyệt bình luận: chuyển về VISIBLE, hiện lại cho mọi người xem.
    AdminCommentReviewDTO approveComment(Long commentId);

    // Từ chối bình luận: giữ ẩn, không hiện lại.
    AdminCommentReviewDTO rejectComment(Long commentId);

    // Admin xác nhận thủ công một tài liệu đủ điều kiện public (bỏ qua/ghi đè kết quả AI).
    void approveDocumentReview(Long documentId);

    // Gỡ xác nhận thủ công đã cấp cho tài liệu.
    void removeDocumentReview(Long documentId);

    // Số liệu tổng quan cho dashboard Admin: tổng user, tổng tài liệu, kháng nghị chờ...
    AdminStatsDTO getStats();

    // Toàn bộ người dùng trong hệ thống, cho trang quản lý người dùng.
    List<AdminUserResponseDTO> getAllUsers();

    // Admin tạo tài khoản mới trực tiếp (không qua luồng đăng ký + OTP).
    AdminUserResponseDTO createUser(AdminCreateUserRequestDTO request);

    // Admin sửa thông tin một người dùng.
    AdminUserResponseDTO updateUser(Long userId, AdminUpdateUserRequestDTO request);

    // Admin đổi trạng thái tài khoản (vd khoá/mở khoá — ACTIVE/BANNED).
    AdminUserResponseDTO updateUserStatus(Long userId, AdminUpdateUserStatusRequestDTO request);

    // Admin đặt lại mật khẩu cho người dùng (hỗ trợ khi họ quên và không truy cập được email).
    void resetUserPassword(Long userId, AdminResetPasswordRequestDTO request);
}
