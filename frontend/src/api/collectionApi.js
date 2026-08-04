import apiClient from "../lib/apiClient";

// Tập trung toàn bộ request liên quan /api/collections/* tại đây.

// Gọi API GET /collections — danh sách bộ sưu tập của tôi.
export function listMyCollections() {
  return apiClient.get("/collections").then((res) => res.data);
}

// Gọi API GET /collections/{id} — chi tiết bộ sưu tập kèm tài liệu bên trong.
export function getOne(id) {
  return apiClient.get(`/collections/${id}`).then((res) => res.data);
}

// Gọi API POST /collections — tạo bộ sưu tập mới.
export function createCollection(name) {
  return apiClient.post("/collections", { name }).then((res) => res.data);
}

// Gọi API PUT /collections/{id} — đổi tên bộ sưu tập.
export function renameCollection(id, name) {
  return apiClient.put(`/collections/${id}`, { name }).then((res) => res.data);
}

// Gọi API DELETE /collections/{id} — xoá bộ sưu tập (không xoá tài liệu gốc).
export function deleteCollection(id) {
  return apiClient.delete(`/collections/${id}`);
}

// Gọi API DELETE /collections/{collectionId}/documents/{docId} — gỡ 1 tài liệu khỏi bộ sưu tập.
export function removeDocument(collectionId, docId) {
  return apiClient.delete(`/collections/${collectionId}/documents/${docId}`);
}

// Gọi API POST /collections/{collectionId}/documents — thêm nhiều tài liệu vào bộ sưu tập cùng lúc.
// documentIds: array of doc ids (backend accepts multi-add in one call).
export function addDocuments(collectionId, documentIds) {
  return apiClient
    .post(`/collections/${collectionId}/documents`, { documentIds })
    .then((res) => res.data);
}
