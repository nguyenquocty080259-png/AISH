import apiClient from "../lib/apiClient";

// Tập trung toàn bộ request liên quan /api/admin/* tại đây.

// Gọi API GET /admin/stats — số liệu tổng quan cho dashboard Admin.
export function getStats() {
  return apiClient.get("/admin/stats").then((res) => res.data);
}

// Gọi API GET /admin/ai-usage — số liệu dùng AI nhanh (hôm nay/7 ngày).
export function getAiUsage() {
  return apiClient.get("/admin/ai-usage").then((res) => res.data);
}

// granularity: "DAY" | "WEEK" | "MONTH" | "QUARTER" | "YEAR". from/to: "YYYY-MM-DD" (to là exclusive
// ở BE - xem AiUsageReport.jsx để biết cách bù +1 ngày trước khi gọi hàm này).
export function getAiUsageReport(granularity, from, to) {
  return apiClient
    .get("/admin/ai-usage/report", { params: { granularity, from, to } })
    .then((res) => res.data);
}

// Gọi API GET /admin/appeals — danh sách kháng cáo kiểm duyệt.
// status: AppealStatus ("APPEAL_PENDING" | "APPEAL_APPROVED" | "APPEAL_REJECTED"), optional.
// Omit to get every appeal regardless of status.
export function listAppeals(status) {
  return apiClient
    .get("/admin/appeals", { params: status ? { status } : {} })
    .then((res) => res.data);
}

// Gọi API POST /admin/appeals/{appealId}/approve — chấp nhận kháng cáo (tài liệu chuyển PUBLIC/APPROVED).
export function approveAppeal(appealId, note) {
  return apiClient
    .post(`/admin/appeals/${appealId}/approve`, note ? { note } : {})
    .then((res) => res.data);
}

// Gọi API POST /admin/appeals/{appealId}/reject — từ chối kháng cáo.
export function rejectAppeal(appealId, note) {
  return apiClient
    .post(`/admin/appeals/${appealId}/reject`, note ? { note } : {})
    .then((res) => res.data);
}

// Gọi API GET /admin/comments?status=PENDING_REVIEW — bình luận đang chờ Admin duyệt.
export function listPendingComments() {
  return apiClient
    .get("/admin/comments", { params: { status: "PENDING_REVIEW" } })
    .then((res) => res.data);
}

// Gọi API POST /admin/comments/{id}/review-approve — duyệt bình luận, hiện lại công khai.
export function approveComment(id) {
  return apiClient.post(`/admin/comments/${id}/review-approve`).then((res) => res.data);
}

// Gọi API POST /admin/comments/{id}/review-reject — từ chối bình luận, giữ ẩn.
export function rejectComment(id) {
  return apiClient.post(`/admin/comments/${id}/review-reject`).then((res) => res.data);
}

// Gọi API GET /admin/reports — danh sách report theo trạng thái (status = null lấy tất cả).
export function listReports(status) {
  return apiClient
    .get("/admin/reports", { params: status ? { status } : {} })
    .then((res) => res.data);
}

// Gọi API PUT /admin/reports/{id}/resolve — xử lý report (payload: { actionTaken, adminResponse }).
export function resolveReport(id, payload) {
  return apiClient.put(`/admin/reports/${id}/resolve`, payload).then((res) => res.data);
}

// Gọi API GET /admin/documents — danh sách tài liệu cho trang quản trị, lọc nhiều tiêu chí.
// Returns a Spring Data Page: { content, totalPages, totalElements, number, size, ... }.
// page is 0-indexed (matches Spring Pageable). Backend default: size=20, sort=createdAt DESC.
// keyword lọc theo tiêu đề; moderationStatus là ModerationStatus ("NOT_REQUIRED" |
// "ADMIN_PENDING" | "APPROVED" | "REJECTED"); removed lọc theo trạng thái gỡ (null = cả hai,
// false = chỉ đang hoạt động, true = chỉ đã gỡ). needsReview = true thì BE bỏ qua các bộ lọc kia.
export function listDocuments(
  page = 0,
  size = 20,
  visibility = null,
  needsReview = false,
  keyword = null,
  moderationStatus = null,
  removed = null
) {
  const params = { page, size };
  if (visibility) {
    params.visibility = visibility;
  }
  if (needsReview) {
    params.needsReview = true;
  }
  if (keyword) {
    params.keyword = keyword;
  }
  if (moderationStatus) {
    params.moderationStatus = moderationStatus;
  }
  // So sánh với null: false là một giá trị lọc hợp lệ ("chỉ đang hoạt động"), không phải "bỏ qua".
  if (removed !== null && removed !== undefined) {
    params.removed = removed;
  }

  return apiClient
    .get("/admin/documents", { params })
    .then((res) => res.data);
}

// Gọi API POST /admin/documents/{id}/review-approve — Admin tự xác nhận tài liệu đủ điều kiện public.
export function approveDocumentReview(id) {
  return apiClient.post(`/admin/documents/${id}/review-approve`);
}

// Gọi API POST /admin/documents/{id}/review-remove — Admin từ chối, chuyển tài liệu về private.
export function removeDocumentReview(id) {
  return apiClient.post(`/admin/documents/${id}/review-remove`);
}

// Gọi API DELETE /admin/documents/{id} — Admin gỡ (xoá) tài liệu vi phạm.
// Takedown — no request body/reason supported by the backend (DELETE only takes the id).
export function removeDocument(id) {
  return apiClient.delete(`/admin/documents/${id}`);
}

// Gọi API PUT /admin/documents/{id} — Admin sửa thông tin tài liệu.
// payload: { title?, description?, subjectIds? } — same shape as the owner-scoped endpoint.
export function updateDocument(id, payload) {
  return apiClient.put(`/admin/documents/${id}`, payload).then((res) => res.data);
}

// Gọi API PUT /admin/documents/{id}/restore — khôi phục tài liệu đã gỡ.
export function restoreDocument(id) {
  return apiClient.put(`/admin/documents/${id}/restore`);
}

// Gọi API GET /admin/users — toàn bộ người dùng, cho trang quản lý người dùng.
export function getAllUsers() {
  return apiClient.get("/admin/users").then((res) => res.data);
}

// Gọi API POST /admin/users — Admin tạo tài khoản mới trực tiếp.
export function createUser(payload) {
  return apiClient.post("/admin/users", payload).then((res) => res.data);
}

// Gọi API PUT /admin/users/{id} — Admin sửa thông tin người dùng.
export function updateUser(id, payload) {
  return apiClient.put(`/admin/users/${id}`, payload).then((res) => res.data);
}

// Gọi API PATCH /admin/users/{id}/status — đổi trạng thái tài khoản (ACTIVE/BANNED).
export function updateUserStatus(id, status) {
  return apiClient
    .patch(`/admin/users/${id}/status`, { status })
    .then((res) => res.data);
}

// Gọi API GET /admin/moderation-keywords — danh sách từ khoá cấm, lọc theo loại.
export function listModerationKeywords(type) {
  return apiClient
    .get("/admin/moderation-keywords", { params: type ? { type } : {} })
    .then((res) => res.data);
}

// Gọi API POST /admin/moderation-keywords — thêm từ khoá cấm mới.
export function createModerationKeyword(payload) {
  return apiClient.post("/admin/moderation-keywords", payload).then((res) => res.data);
}

// Gọi API PUT /admin/moderation-keywords/{id} — sửa/bật-tắt một từ khoá.
export function updateModerationKeyword(id, payload) {
  return apiClient.put(`/admin/moderation-keywords/${id}`, payload).then((res) => res.data);
}

// Gọi API DELETE /admin/moderation-keywords/{id} — xoá từ khoá.
export function deleteModerationKeyword(id) {
  return apiClient.delete(`/admin/moderation-keywords/${id}`);
}

// Gọi API GET /admin/settings/min-upload-age — tuổi tối thiểu được phép upload.
export function getMinUploadAge() {
  return apiClient.get("/admin/settings/min-upload-age").then((res) => res.data);
}

// Gọi API PUT /admin/settings/min-upload-age — cập nhật tuổi tối thiểu.
export function updateMinUploadAge(minUploadAge) {
  return apiClient
    .put("/admin/settings/min-upload-age", { minUploadAge })
    .then((res) => res.data);
}

// Gọi API GET /admin/settings/upload-limits — giới hạn dung lượng file/quota hiện tại.
// Byte - quy đổi GB chỉ ở FE (xem useAdminSettingsPage.js). maxFile* = giới hạn 1 file;
// quota* = tổng dung lượng cho phép của 1 user (chung cho mọi user).
export function getUploadLimits() {
  return apiClient.get("/admin/settings/upload-limits").then((res) => res.data);
}

// Gọi API PUT /admin/settings/upload-limits — cập nhật giới hạn dung lượng file/quota.
export function updateUploadLimits({ maxFileLocalBytes, maxFileCloudBytes, quotaLocalBytes, quotaCloudBytes }) {
  return apiClient
    .put("/admin/settings/upload-limits", { maxFileLocalBytes, maxFileCloudBytes, quotaLocalBytes, quotaCloudBytes })
    .then((res) => res.data);
}

// Gọi API GET /admin/settings/upload-file-types — danh sách đuôi tệp được phép tải lên.
// Whitelist đuôi tệp được phép tải lên (áp dụng chung mọi user). BE chuẩn hoá (viết thường, bỏ
// dấu chấm, bỏ trùng) và trả về mảng đã chuẩn hoá. { allowedExtensions: string[] }.
export function getUploadFileTypes() {
  return apiClient.get("/admin/settings/upload-file-types").then((res) => res.data);
}

// Gọi API PUT /admin/settings/upload-file-types — cập nhật danh sách đuôi tệp cho phép.
export function updateUploadFileTypes(allowedExtensions) {
  return apiClient
    .put("/admin/settings/upload-file-types", { allowedExtensions })
    .then((res) => res.data);
}

// Gọi API GET /admin/settings/ai-config — tham số AI hiện tại.
// TOP_K/threshold/recent-limit (AiChatService), 4 trọng số gợi ý (RecommendationServiceImpl),
// chunkSize (DocEmbeddingServiceImpl). Tất cả đọc/ghi qua SystemSetting - xem AiConfigDTO.
export function getAiConfig() {
  return apiClient.get("/admin/settings/ai-config").then((res) => res.data);
}

// Gọi API PUT /admin/settings/ai-config — cập nhật tham số AI.
export function updateAiConfig(payload) {
  return apiClient.put("/admin/settings/ai-config", payload).then((res) => res.data);
}

