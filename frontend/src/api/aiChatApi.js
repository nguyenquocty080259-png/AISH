import apiClient from "../lib/apiClient";

// POST /api/ai/chat - public, không cần đăng nhập (permitAll bên backend)
export function chat({ message, documentId = null, conversationId = null }) {
  return apiClient
    .post("/ai/chat", { message, documentId, conversationId })
    .then((res) => res.data);
}

// Gọi API GET /ai/conversations — danh sách cuộc trò chuyện của tôi.
export function getConversations() {
  return apiClient.get("/ai/conversations").then((res) => res.data);
}

// Gọi API GET /ai/conversations/{conversationId}/messages — tin nhắn trong một cuộc trò chuyện.
export function getMessages(conversationId) {
  return apiClient.get(`/ai/conversations/${conversationId}/messages`).then((res) => res.data);
}

// Gọi API DELETE /ai/conversations/{id} — xoá cuộc trò chuyện.
export function deleteConversation(id) {
  return apiClient.delete(`/ai/conversations/${id}`);
}

// Gọi API PUT /ai/conversations/{id} — đổi tên cuộc trò chuyện.
export function renameConversation(id, title) {
  return apiClient.put(`/ai/conversations/${id}`, { title }).then((res) => res.data);
}
