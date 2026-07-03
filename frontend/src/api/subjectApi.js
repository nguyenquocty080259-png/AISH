import apiClient from "../lib/apiClient";

export function getAll() {
  return apiClient.get("/subjects").then((res) => res.data);
}

export function create(subject) {
  // subject: { name, description? } - backend trả về subject đã có nếu trùng tên
  return apiClient.post("/subjects", subject).then((res) => res.data);
}

export function update(id, subject) {
  // subject: { name, description? }
  return apiClient.put(`/subjects/${id}`, subject).then((res) => res.data);
}

export function remove(id) {
  // 409 nếu subject đang được gán cho tài liệu (DEC-030)
  return apiClient.delete(`/subjects/${id}`);
}
