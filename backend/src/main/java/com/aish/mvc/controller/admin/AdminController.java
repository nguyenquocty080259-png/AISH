package com.aish.mvc.controller.admin;

import com.aish.mvc.dto.auth.admin.AdminCreateUserRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminResetPasswordRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminUpdateUserRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminUpdateUserStatusRequestDTO;
import com.aish.mvc.dto.auth.admin.AdminUserResponseDTO;
import com.aish.mvc.dto.doc.AdminAppealResponseDTO;
import com.aish.mvc.dto.doc.AdminDocumentSummaryDTO;
import com.aish.mvc.dto.doc.AdminStatsDTO;
import com.aish.mvc.dto.doc.AdminCommentReviewDTO;
import com.aish.mvc.dto.doc.AppealDecisionRequestDTO;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.dto.doc.DocumentUpdateRequestDTO;
import com.aish.mvc.dto.report.AdminReportResponseDTO;
import com.aish.mvc.dto.report.ResolveReportRequestDTO;
import com.aish.mvc.entity.enums.AppealStatus;
import com.aish.mvc.entity.enums.CommentStatus;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.entity.enums.ReportStatus;
import com.aish.mvc.service.admin.AdminService;
import com.aish.mvc.service.doc.DocumentService;
import com.aish.mvc.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CỬA NGÕ API TRUNG TÂM của trang quản trị: report, kháng cáo, bình luận chờ duyệt, quản lý tài
 * liệu, thống kê, quản lý người dùng. Mọi route ở đây nằm dưới /api/admin/** -> SecurityConfig đã
 * gate .hasRole("ADMIN"), nên chỉ cần đăng nhập với vai trò ADMIN mới gọi được (không cần
 * @PreAuthorize lặp lại ở từng method, dù một số method vẫn có thêm cho rõ ràng).
 */
// Mọi route ở đây nằm dưới /api/admin/** -> SecurityConfig đã gate .hasRole("ADMIN").
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final DocumentService documentService;
    private final ReportService reportService;

    // ===== Report (báo cáo vi phạm) =====
    @GetMapping("/reports")
    public ResponseEntity<List<AdminReportResponseDTO>> listReports(
            @RequestParam(value = "status", required = false) ReportStatus status) {
        return ResponseEntity.ok(reportService.listReports(status));
    }

    @PutMapping("/reports/{id}/resolve")
    public ResponseEntity<AdminReportResponseDTO> resolveReport(
            @PathVariable Long id,
            @RequestBody ResolveReportRequestDTO request) {
        return ResponseEntity.ok(reportService.resolveReport(
                id, request.getActionTaken(), request.getAdminResponse()));
    }

    // ===== Kháng cáo kiểm duyệt =====
    @GetMapping("/appeals")
    public ResponseEntity<List<AdminAppealResponseDTO>> listAppeals(
            @RequestParam(value = "status", required = false) AppealStatus status) {
        return ResponseEntity.ok(adminService.listAppeals(status));
    }

    @PostMapping("/appeals/{appealId}/approve")
    public ResponseEntity<AdminAppealResponseDTO> approveAppeal(
            @PathVariable Long appealId,
            @RequestBody(required = false) AppealDecisionRequestDTO body) {
        String note = body != null ? body.getNote() : null;
        return ResponseEntity.ok(adminService.approveAppeal(appealId, note));
    }

    @PostMapping("/appeals/{appealId}/reject")
    public ResponseEntity<AdminAppealResponseDTO> rejectAppeal(
            @PathVariable Long appealId,
            @RequestBody(required = false) AppealDecisionRequestDTO body) {
        String note = body != null ? body.getNote() : null;
        return ResponseEntity.ok(adminService.rejectAppeal(appealId, note));
    }

    // ===== Bình luận chờ duyệt (AI đã gắn cờ) =====
    @GetMapping("/comments")
    public ResponseEntity<List<AdminCommentReviewDTO>> listComments(
            @RequestParam(defaultValue = "PENDING_REVIEW") CommentStatus status) {
        return ResponseEntity.ok(adminService.listComments(status));
    }

    @PostMapping("/comments/{id}/review-approve")
    public ResponseEntity<AdminCommentReviewDTO> approveComment(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approveComment(id));
    }

    @PostMapping("/comments/{id}/review-reject")
    public ResponseEntity<AdminCommentReviewDTO> rejectComment(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.rejectComment(id));
    }

    // ===== Quản lý tài liệu (danh sách lọc nhiều tiêu chí, duyệt/từ chối, xoá, sửa, khôi phục) =====
    @GetMapping("/documents")
    public ResponseEntity<Page<AdminDocumentSummaryDTO>> listDocuments(

            @RequestParam(required = false)
            DocumentVisibility visibility,

            @RequestParam(defaultValue = "false")
            boolean needsReview,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            ModerationStatus moderationStatus,

            // null = cả đang hoạt động lẫn đã gỡ; false = chỉ đang hoạt động; true = chỉ đã gỡ.
            @RequestParam(required = false)
            Boolean removed,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(
                documentService.getAllDocumentsForAdmin(
                        visibility,
                        needsReview,
                        keyword,
                        moderationStatus,
                        removed,
                        pageable
                )
        );

    }

    @PostMapping("/documents/{id}/review-approve")
    public ResponseEntity<Void> approveDocumentReview(@PathVariable Long id) {
        adminService.approveDocumentReview(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/documents/{id}/review-remove")
    public ResponseEntity<Void> removeDocumentReview(@PathVariable Long id) {
        adminService.removeDocumentReview(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.adminDeleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/documents/{id}")
    public ResponseEntity<DocumentResponseDTO> updateDocument(
            @PathVariable Long id,
            @RequestBody DocumentUpdateRequestDTO request) {
        return ResponseEntity.ok(
                documentService.adminUpdateDocument(id, request.getTitle(), request.getDescription(), request.getSubjectIds()));
    }

    @PutMapping("/documents/{id}/restore")
    public ResponseEntity<Void> restoreDocument(@PathVariable Long id) {
        documentService.adminRestoreDocument(id);
        return ResponseEntity.ok().build();
    }

    // ===== Thống kê tổng quan =====
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // ===== Quản lý người dùng =====
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponseDTO> createUser(
            @Valid @RequestBody AdminCreateUserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequestDTO request) {
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponseDTO> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserStatusRequestDTO request) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, request));
    }

    @PatchMapping("/users/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetUserPassword(
            @PathVariable Long id,
            @Valid @RequestBody AdminResetPasswordRequestDTO request) {
        adminService.resetUserPassword(id, request);
        return ResponseEntity.noContent().build();
    }
}
