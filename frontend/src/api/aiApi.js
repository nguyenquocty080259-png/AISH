import apiClient from "../lib/apiClient";

// Các request /api/ai/* KHÔNG phải chat (chat nằm ở aiChatApi.js).

// POST /api/ai/ingest/{documentId} - yêu cầu đăng nhập, chỉ chủ tài liệu/admin (backend tự kiểm tra).
// Luôn trả 200 kèm { documentId, status, chunkCount, message } - kể cả khi KHÔNG ingest được
// (vd status "SKIPPED_NON_PDF"), nên phải đọc field status trong response chứ không chỉ dựa vào catch().
export function ingest(documentId) {
  return apiClient.post(`/ai/ingest/${documentId}`).then((res) => res.data);
}
