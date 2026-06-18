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

export function upload(formData) {
  // formData: title, description, subjectId?, tags[]?, file
  return apiClient.post("/documents/upload", formData).then((res) => res.data);
}

export function toggleFavorite(id) {
  return apiClient.post(`/documents/${id}/favorite`);
}

export function addComment(id, content) {
  // Backend nhận @RequestBody String content (chuỗi thô, không phải object)
  return apiClient.post(`/documents/${id}/comment`, content);
}

export function rateDocument(id, star) {
  // Backend nhận star qua query param, không phải body
  return apiClient.post(`/documents/${id}/rate`, null, { params: { star } });
}

export function deleteDocument(id) {
  return apiClient.delete(`/documents/${id}`);
}

export function restoreDocument(id) {
  return apiClient.put(`/documents/${id}/restore`);
}

export function toggleVisibility(id) {
  return apiClient.put(`/documents/${id}/toggle-visibility`);
}

export function downloadFile(id) {
  // Trả về cả response (không chỉ data) để lấy header Content-Disposition
  return apiClient.get(`/documents/${id}/download`, { responseType: "blob" });
}
