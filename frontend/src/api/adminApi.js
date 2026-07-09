import apiClient from "../lib/apiClient";

// Tập trung toàn bộ request liên quan /api/admin/* tại đây.

export function getStats() {
  return apiClient.get("/admin/stats").then((res) => res.data);
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

// Returns a Spring Data Page: { content, totalPages, totalElements, number, size, ... }.
// page is 0-indexed (matches Spring Pageable). Backend default: size=20, sort=createdAt DESC.
export function listDocuments(page = 0, size = 20) {
  return apiClient.get("/admin/documents", { params: { page, size } }).then((res) => res.data);
}

// Takedown — no request body/reason supported by the backend (DELETE only takes the id).
export function removeDocument(id) {
  return apiClient.delete(`/admin/documents/${id}`);
}

export function getAllUsers() {
  return apiClient.get("/admin/users").then((res) => res.data);
}
