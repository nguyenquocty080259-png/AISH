import apiClient from "../lib/apiClient";

export function getAll() {
  return apiClient.get("/subjects").then((res) => res.data);
}

export function create(subject) {
  // subject: { name, description? } - backend trả về subject đã có nếu trùng tên
  return apiClient.post("/subjects", subject).then((res) => res.data);
}
