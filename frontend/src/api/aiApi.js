import apiClient from "../lib/apiClient";

// Các request /api/ai/* KHÔNG phải chat (chat nằm ở aiChatApi.js).

// POST /api/ai/ingest/{documentId} - yêu cầu đăng nhập, chỉ chủ tài liệu/admin (backend tự kiểm tra).
// Luôn trả 200 kèm { documentId, status, chunkCount, message } - kể cả khi KHÔNG ingest được
// (vd status "SKIPPED_NON_PDF"), nên phải đọc field status trong response chứ không chỉ dựa vào catch().
export function ingest(documentId) {
  return apiClient.post(`/ai/ingest/${documentId}`).then((res) => res.data);
}

// GET /api/ai/recommendations?documentId=... - tài liệu PUBLIC liên quan tới 1 tài liệu cho trước.
// Trả về List<RecommendedDocumentDTO>: { documentId, title, ownerName, subjectNames, favoriteCount,
// downloadCount, averageRating, score }. Không có storageType.
export function getRelatedDocuments(documentId) {
  return apiClient
    .get("/ai/recommendations", { params: { documentId } })
    .then((res) => res.data);
}

// GET /api/ai/recommendations (không kèm documentId) - gợi ý "Dành cho bạn" dựa trên tín hiệu cá nhân.
// Cùng shape RecommendedDocumentDTO như trên.
export function getRecommendationsForYou() {
  return apiClient.get("/ai/recommendations").then((res) => res.data);
}
