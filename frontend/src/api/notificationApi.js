import apiClient from "../lib/apiClient";

// Gọi API GET /notifications/mine — danh sách thông báo của tôi, có phân trang.
export function getMyNotifications(page = 0, size = 15) {
  return apiClient.get("/notifications/mine", { params: { page, size } }).then((res) => res.data);
}

// Gọi API GET /notifications/unread-count — số thông báo chưa đọc (hiện số đỏ trên chuông).
export function getUnreadCount() {
  return apiClient.get("/notifications/unread-count").then((res) => res.data);
}

// Gọi API PUT /notifications/{id}/read — đánh dấu một thông báo đã đọc.
export function markAsRead(id) {
  return apiClient.put(`/notifications/${id}/read`).then((res) => res.data);
}

// Gọi API PUT /notifications/read-all — đánh dấu tất cả đã đọc.
export function markAllAsRead() {
  return apiClient.put("/notifications/read-all");
}

// Gọi API DELETE /notifications/{id} — xoá một thông báo.
export function deleteNotification(id) {
  return apiClient.delete(`/notifications/${id}`);
}

// Gọi API DELETE /notifications/clear-all — xoá toàn bộ thông báo của tôi.
export function clearAllNotifications() {
  return apiClient.delete("/notifications/clear-all");
}

// Gọi API GET /notifications/preferences — cài đặt loại thông báo đang bật/tắt.
export function getNotificationPreferences() {
  return apiClient.get("/notifications/preferences").then((res) => res.data);
}

// Gọi API PUT /notifications/preferences — cập nhật cài đặt loại thông báo muốn nhận.
export function updateNotificationPreferences(prefs) {
  return apiClient.put("/notifications/preferences", prefs);
}
