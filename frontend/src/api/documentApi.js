import apiClient from "../lib/apiClient";

// Tập trung toàn bộ request liên quan /api/documents/* tại đây.

export function getAll() {
  return apiClient.get("/documents").then((res) => res.data);
}

export function getOne(id) {
  return apiClient.get(`/documents/${id}`).then((res) => res.data);
}

export function getTrash() {
  return apiClient.get("/documents/trash").then((res) => res.data);
}

export function listFavorites() {
  return apiClient.get("/documents/favorites").then((res) => res.data);
}

export function getCommunity(params) {
  // params: { keyword, subjectId, tagId, minRating, sortBy, page, size }
  return apiClient.get("/documents/community", { params }).then((res) => res.data);
}

export function previewFile(id) {
  return apiClient
    .get(`/documents/${id}/preview`, { responseType: "blob" })
    .then((res) => res.data);
}

// Endpoint hợp nhất — formData nên kèm field "storage": LOCAL | CLOUD | BOTH
// (không gửi thì BE mặc định LOCAL, tương thích ngược).
export function upload(formData, onProgress) {
  return apiClient
    .post("/documents/upload", formData, { onUploadProgress: onProgress })
    .then((res) => res.data);
}

// Byte - dùng để chặn trước ở FE trước khi gửi file lớn lên server (xem UploadModal.jsx).
export function getUploadLimits() {
  return apiClient.get("/documents/upload-limits").then((res) => res.data);
}

export function updateDocument(id, data) {
  // data: { title?, description?, subjectIds? }  (field bỏ trống = giữ nguyên)
  return apiClient.put(`/documents/${id}`, data).then((res) => res.data);
}

export function toggleFavorite(id) {
  return apiClient.post(`/documents/${id}/favorite`);
}
export function addComment(id, content, { dispute = false, disputeNote } = {}) {
  return apiClient.post(`/documents/${id}/comment`, content, {
    headers: { "Content-Type": "text/plain; charset=UTF-8" },
    params: { dispute, ...(disputeNote ? { disputeNote } : {}) },
  });
}

export function updateComment(commentId, content, { dispute = false, disputeNote } = {}) {
  return apiClient.put(`/documents/comments/${commentId}`, content, {
    headers: { "Content-Type": "text/plain; charset=UTF-8" },
    params: { dispute, ...(disputeNote ? { disputeNote } : {}) },
  });
}
export function deleteComment(commentId) {
  return apiClient.delete(`/documents/comments/${commentId}`);
}

export function rateDocument(id, star) {
  return apiClient.post(`/documents/${id}/rate`, null, { params: { star } });
}

export function deleteDocument(id) {
  return apiClient.delete(`/documents/${id}`);
}

export function permanentDelete(id) {
  return apiClient.delete(`/documents/${id}/permanent`);
}

export function restoreDocument(id) {
  return apiClient.put(`/documents/${id}/restore`);
}

// BE giờ trả về document sau khi đổi (visibility mới + moderationStatus + moderationReason)
// để FE hiển thị kết quả kiểm duyệt AI thay vì im lặng.
export function toggleVisibility(id) {
  return apiClient
    .put(`/documents/${id}/toggle-visibility`)
    .then((res) => res.data);
}

export function downloadFile(id) {
  return apiClient.get(`/documents/${id}/download`, { responseType: "blob" });
}
