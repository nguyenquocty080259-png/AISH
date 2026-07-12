import apiClient from "../lib/apiClient";

export function getMyNotifications() {
  return apiClient.get("/notifications/mine").then((res) => res.data);
}

export function getUnreadCount() {
  return apiClient.get("/notifications/unread-count").then((res) => res.data);
}

export function markAsRead(id) {
  return apiClient.put(`/notifications/${id}/read`).then((res) => res.data);
}

export function markAllAsRead() {
  return apiClient.put("/notifications/read-all");
}
