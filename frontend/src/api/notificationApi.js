import apiClient from "../lib/apiClient";

export function getMyNotifications(page = 0, size = 15) {
  return apiClient.get("/notifications/mine", { params: { page, size } }).then((res) => res.data);
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

export function deleteNotification(id) {
  return apiClient.delete(`/notifications/${id}`);
}

export function clearAllNotifications() {
  return apiClient.delete("/notifications/clear-all");
}

export function getNotificationPreferences() {
  return apiClient.get("/notifications/preferences").then((res) => res.data);
}

export function updateNotificationPreferences(prefs) {
  return apiClient.put("/notifications/preferences", prefs);
}
