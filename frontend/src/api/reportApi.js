import apiClient from "../lib/apiClient";

// Gọi API POST /reports — gửi báo cáo vi phạm mới.
export function createReport(payload) {
  return apiClient.post("/reports", payload).then((res) => res.data);
}

// Gọi API GET /reports/mine — danh sách report tôi đã gửi.
export function getMyReports() {
  return apiClient.get("/reports/mine").then((res) => res.data);
}
