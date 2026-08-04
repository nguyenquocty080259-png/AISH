import apiClient from "../lib/apiClient";

// Tập trung toàn bộ request liên quan "Tiếp tục học" (/api/users/recently-viewed) tại đây.

// Gọi API GET /users/recently-viewed — danh sách tài liệu xem gần đây.
export function getRecentlyViewed() {
  return apiClient.get("/users/recently-viewed").then((res) => res.data);
}
