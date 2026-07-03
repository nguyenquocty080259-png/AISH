import apiClient from "../lib/apiClient";

// Tập trung toàn bộ request liên quan /api/collections/* tại đây.

export function listMyCollections() {
  return apiClient.get("/collections").then((res) => res.data);
}

export function getOne(id) {
  return apiClient.get(`/collections/${id}`).then((res) => res.data);
}

export function createCollection(name) {
  return apiClient.post("/collections", { name }).then((res) => res.data);
}

export function renameCollection(id, name) {
  return apiClient.put(`/collections/${id}`, { name }).then((res) => res.data);
}

export function deleteCollection(id) {
  return apiClient.delete(`/collections/${id}`);
}

export function removeDocument(collectionId, docId) {
  return apiClient.delete(`/collections/${collectionId}/documents/${docId}`);
}

// documentIds: array of doc ids (backend accepts multi-add in one call).
export function addDocuments(collectionId, documentIds) {
  return apiClient
    .post(`/collections/${collectionId}/documents`, { documentIds })
    .then((res) => res.data);
}
