import apiClient from "../lib/apiClient";

// Gọi API GET /appeals/mine — danh sách kháng cáo kiểm duyệt của tôi.
export function getMyAppeals() {
  return apiClient.get("/appeals/mine").then((res) => res.data);
}
