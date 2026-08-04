import apiClient from "../lib/apiClient";

// Gọi API GET /cases/{type}/{id}/messages — tin nhắn trao đổi trong một "case" (khiếu nại/hỗ trợ).
export function getCaseMessages(type, id) {
  return apiClient.get(`/cases/${type}/${id}/messages`).then((r) => r.data);
}

// Gọi API POST /cases/{type}/{id}/messages — gửi tin nhắn mới trong case.
export function postCaseMessage(type, id, content) {
  return apiClient.post(`/cases/${type}/${id}/messages`, { content }).then((r) => r.data);
}
