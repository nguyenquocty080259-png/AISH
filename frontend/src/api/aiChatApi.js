import apiClient from "../lib/apiClient";

// POST /api/ai/chat - public, không cần đăng nhập (permitAll bên backend)
export function chat({ message, documentId = null, conversationId = null }) {
  return apiClient
    .post("/ai/chat", { message, documentId, conversationId })
    .then((res) => res.data);
}
