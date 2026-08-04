import apiClient from "../lib/apiClient";

// Tập trung request liên quan chia sẻ tài liệu (/api/documents/*/share, /shared-with-me).

// Gọi API POST /documents/{id}/share — chia sẻ tài liệu.
// body: { mode: "RESTRICTED" | "ANYONE_WITH_LINK" | "NONE", email?: string, userIds?: number[], permission?: "VIEWER" | "COMMENTER" }
// RESTRICTED dùng email (người nhận đã có tài khoản HiveMind). Trả về { shareMode, shareToken } —
// shareToken chỉ có khi mode = ANYONE_WITH_LINK.
export function shareDocument(id, body) {
  return apiClient.post(`/documents/${id}/share`, body).then((res) => res.data);
}

// Gọi API GET /documents/shared-with-me — Danh sách tài liệu được chia sẻ với tôi: [{ document, permission, sharedByName }]
export function getSharedWithMe() {
  return apiClient.get("/documents/shared-with-me").then((res) => res.data);
}

// Gọi API GET /documents/{id}/shares — CHỈ chủ sở hữu: danh sách người đang được chia sẻ tài liệu: [{ userId, fullName, email, permission }]
export function getShareRecipients(id) {
  return apiClient.get(`/documents/${id}/shares`).then((res) => res.data);
}

// Gọi API DELETE /documents/{id}/share/{userId} — thu hồi quyền chia sẻ của một người.
export function revokeShare(id, userId) {
  return apiClient.delete(`/documents/${id}/share/${userId}`);
}
