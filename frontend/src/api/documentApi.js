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

export function getCommunity(params) {
  // params: { keyword, subjectId, sortBy, page, size }
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

export function toggleFavorite(id) {
  return apiClient.post(`/documents/${id}/favorite`);
}

export function addComment(id, content) {
  return apiClient.post(`/documents/${id}/comment`, content);
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