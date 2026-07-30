import apiClient from "../lib/apiClient";

// Tập trung toàn bộ request liên quan /api/admin/* tại đây.

export function getStats() {
  return apiClient.get("/admin/stats").then((res) => res.data);
}

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

// status: AppealStatus ("APPEAL_PENDING" | "APPEAL_APPROVED" | "APPEAL_REJECTED"), optional.
// Omit to get every appeal regardless of status.
export function listAppeals(status) {
  return apiClient
    .get("/admin/appeals", { params: status ? { status } : {} })
    .then((res) => res.data);
}

export function approveAppeal(appealId, note) {
  return apiClient
    .post(`/admin/appeals/${appealId}/approve`, note ? { note } : {})
    .then((res) => res.data);
}

export function rejectAppeal(appealId, note) {
  return apiClient
    .post(`/admin/appeals/${appealId}/reject`, note ? { note } : {})
    .then((res) => res.data);
}

export function listPendingComments() {
  return apiClient
    .get("/admin/comments", { params: { status: "PENDING_REVIEW" } })
    .then((res) => res.data);
}

export function approveComment(id) {
  return apiClient.post(`/admin/comments/${id}/review-approve`).then((res) => res.data);
}

export function rejectComment(id) {
  return apiClient.post(`/admin/comments/${id}/review-reject`).then((res) => res.data);
}

export function listReports(status) {
  return apiClient
    .get("/admin/reports", { params: status ? { status } : {} })
    .then((res) => res.data);
}

export function resolveReport(id, payload) {
  return apiClient.put(`/admin/reports/${id}/resolve`, payload).then((res) => res.data);
}

// Returns a Spring Data Page: { content, totalPages, totalElements, number, size, ... }.
// page is 0-indexed (matches Spring Pageable). Backend default: size=20, sort=createdAt DESC.
export function listDocuments(page = 0, size = 20, visibility = null, needsReview = false) {
  const params = { page, size };
  if (visibility) {
    params.visibility = visibility;
  }
  if (needsReview) {
    params.needsReview = true;
  }

  return apiClient
    .get("/admin/documents", { params })
    .then((res) => res.data);
}

export function approveDocumentReview(id) {
  return apiClient.post(`/admin/documents/${id}/review-approve`);
}

export function removeDocumentReview(id) {
  return apiClient.post(`/admin/documents/${id}/review-remove`);
}

// Takedown — no request body/reason supported by the backend (DELETE only takes the id).
export function removeDocument(id) {
  return apiClient.delete(`/admin/documents/${id}`);
}

// payload: { title?, description?, subjectIds? } — same shape as the owner-scoped endpoint.
export function updateDocument(id, payload) {
  return apiClient.put(`/admin/documents/${id}`, payload).then((res) => res.data);
}

export function restoreDocument(id) {
  return apiClient.put(`/admin/documents/${id}/restore`);
}

export function getAllUsers() {
  return apiClient.get("/admin/users").then((res) => res.data);
}

export function createUser(payload) {
  return apiClient.post("/admin/users", payload).then((res) => res.data);
}

export function updateUser(id, payload) {
  return apiClient.put(`/admin/users/${id}`, payload).then((res) => res.data);
}

export function updateUserStatus(id, status) {
  return apiClient
    .patch(`/admin/users/${id}/status`, { status })
    .then((res) => res.data);
}

export function listModerationKeywords(type) {
  return apiClient
    .get("/admin/moderation-keywords", { params: type ? { type } : {} })
    .then((res) => res.data);
}

export function createModerationKeyword(payload) {
  return apiClient.post("/admin/moderation-keywords", payload).then((res) => res.data);
}

export function updateModerationKeyword(id, payload) {
  return apiClient.put(`/admin/moderation-keywords/${id}`, payload).then((res) => res.data);
}

export function deleteModerationKeyword(id) {
  return apiClient.delete(`/admin/moderation-keywords/${id}`);
}

export function getMinUploadAge() {
  return apiClient.get("/admin/settings/min-upload-age").then((res) => res.data);
}

export function updateMinUploadAge(minUploadAge) {
  return apiClient
    .put("/admin/settings/min-upload-age", { minUploadAge })
    .then((res) => res.data);
}

// Byte - quy đổi GB chỉ ở FE (xem useAdminSettingsPage.js). maxFile* = giới hạn 1 file;
// quota* = tổng dung lượng cho phép của 1 user (chung cho mọi user).
export function getUploadLimits() {
  return apiClient.get("/admin/settings/upload-limits").then((res) => res.data);
}

export function updateUploadLimits({ maxFileLocalBytes, maxFileCloudBytes, quotaLocalBytes, quotaCloudBytes }) {
  return apiClient
    .put("/admin/settings/upload-limits", { maxFileLocalBytes, maxFileCloudBytes, quotaLocalBytes, quotaCloudBytes })
    .then((res) => res.data);
}

// Whitelist đuôi tệp được phép tải lên (áp dụng chung mọi user). BE chuẩn hoá (viết thường, bỏ
// dấu chấm, bỏ trùng) và trả về mảng đã chuẩn hoá. { allowedExtensions: string[] }.
export function getUploadFileTypes() {
  return apiClient.get("/admin/settings/upload-file-types").then((res) => res.data);
}

export function updateUploadFileTypes(allowedExtensions) {
  return apiClient
    .put("/admin/settings/upload-file-types", { allowedExtensions })
    .then((res) => res.data);
}

// TOP_K/threshold/recent-limit (AiChatService), 4 trọng số gợi ý (RecommendationServiceImpl),
// chunkSize (DocEmbeddingServiceImpl). Tất cả đọc/ghi qua SystemSetting - xem AiConfigDTO.
export function getAiConfig() {
  return apiClient.get("/admin/settings/ai-config").then((res) => res.data);
}

export function updateAiConfig(payload) {
  return apiClient.put("/admin/settings/ai-config", payload).then((res) => res.data);
}

