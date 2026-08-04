import apiClient from "../lib/apiClient";

// Gọi API GET /interactions/summary — tóm tắt tương tác của tôi (bình luận/yêu thích/đánh giá...).
export function getInteractionSummary() {
  return apiClient.get("/interactions/summary").then((res) => res.data);
}
