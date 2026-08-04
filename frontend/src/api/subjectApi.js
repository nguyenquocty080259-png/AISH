import apiClient from "../lib/apiClient";

// Gọi API GET /subjects — toàn bộ môn học (đổ vào ô chọn môn, bộ lọc trang Cộng đồng).
export function getAll() {
  return apiClient.get("/subjects").then((res) => res.data);
}

// Gọi API POST /subjects — tạo môn học mới (chỉ Admin).
export function create(subject) {
  // subject: { name, description? } - backend trả về subject đã có nếu trùng tên
  return apiClient.post("/subjects", subject).then((res) => res.data);
}

// Gọi API PUT /subjects/{id} — sửa tên/mô tả môn học (chỉ Admin).
export function update(id, subject) {
  // subject: { name, description? }
  return apiClient.put(`/subjects/${id}`, subject).then((res) => res.data);
}

// Gọi API DELETE /subjects/{id} — xoá môn học (chỉ Admin).
export function remove(id) {
  // 409 nếu subject đang được gán cho tài liệu (DEC-030)
  return apiClient.delete(`/subjects/${id}`);
}
