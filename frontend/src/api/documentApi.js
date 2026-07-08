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

export function upload(formData, onProgress) {
  return apiClient
    .post("/documents/upload-server", formData, { onUploadProgress: onProgress })
    .then((res) => res.data);
}

export function updateDocument(id, data) {
  // data: { title?, description?, subjectIds? }  (field bỏ trống = giữ nguyên)
  return apiClient.put(`/documents/${id}`, data).then((res) => res.data);
}

export function toggleFavorite(id) {
  return apiClient.post(`/documents/${id}/favorite`);
}

export function addComment(id, content) {
  return apiClient.post(`/documents/${id}/comment`, content);
}

export function updateComment(commentId, content) {
  return apiClient.put(`/documents/comments/${commentId}`, content);
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

export function toggleVisibility(id) {
  return apiClient.put(`/documents/${id}/toggle-visibility`);
}

export function downloadFile(id) {
  return apiClient.get(`/documents/${id}/download`, { responseType: "blob" });
}