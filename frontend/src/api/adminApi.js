import apiClient from "../lib/apiClient";

// Tập trung toàn bộ request liên quan /api/admin/* tại đây.

export function getStats() {
  return apiClient.get("/admin/stats").then((res) => res.data);
}

export function getAiUsage() {
  return apiClient.get("/admin/ai-usage").then((res) => res.data);
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

