import apiClient from "../lib/apiClient";

export function getCaseMessages(type, id) {
  return apiClient.get(`/cases/${type}/${id}/messages`).then((r) => r.data);
}

export function postCaseMessage(type, id, content) {
  return apiClient.post(`/cases/${type}/${id}/messages`, { content }).then((r) => r.data);
}
