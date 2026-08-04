import apiClient from "../lib/apiClient";

// Tập trung toàn bộ request liên quan /api/documents/* tại đây.
// Mỗi hàm gọi 1 API tương ứng ở DocumentController (backend) rồi trả về res.data cho component dùng.

// Gọi API GET /documents — danh sách tài liệu của tôi.
export function getAll() {
  return apiClient.get("/documents").then((res) => res.data);
}

// Gọi API GET /documents/{id} — chi tiết một tài liệu.
export function getOne(id) {
  return apiClient.get(`/documents/${id}`).then((res) => res.data);
}

// Gọi API GET /documents/trash — danh sách tài liệu trong thùng rác.
export function getTrash() {
  return apiClient.get("/documents/trash").then((res) => res.data);
}

// Gọi API GET /documents/favorites — danh sách tài liệu đã yêu thích.
export function listFavorites() {
  return apiClient.get("/documents/favorites").then((res) => res.data);
}

// Gọi API GET /documents/community — trang Cộng đồng, có lọc/phân trang.
export function getCommunity(params) {
  // params: { keyword, subjectId, minRating, sortBy, page, size }
  return apiClient.get("/documents/community", { params }).then((res) => res.data);
}

// Gọi API GET /documents/{id}/preview — lấy file dạng blob để xem trước trong trang.
export function previewFile(id) {
  return apiClient
    .get(`/documents/${id}/preview`, { responseType: "blob" })
    .then((res) => res.data);
}

// Ảnh thumbnail do BE sinh sẵn (không phải file gốc) — có xác thực, quyền giống /preview
// (owner hoặc PUBLIC). BE trả 204 khi tài liệu không có thumbnail (docx/pptx/txt, hoặc sinh
// thumbnail lúc upload thất bại), quy về null cho FE dễ fallback (xem DocumentThumb.jsx).
export function getThumbnail(id) {
  return apiClient
    .get(`/documents/${id}/thumbnail`, { responseType: "blob" })
    .then((res) => (res.status === 204 ? null : res.data));
}

// Text trích xuất best-effort (DOCX/PPTX/khác) cho ExtractedTextViewer — BE trả 204 khi
// không trích được gì (file rỗng/hỏng/có mật khẩu), ở đây quy về null cho FE dễ xử lý.
export function previewText(id) {
  return apiClient
    .get(`/documents/${id}/preview-text`, { responseType: "text" })
    .then((res) => (res.status === 204 ? null : res.data));
}

// Endpoint hợp nhất — formData nên kèm field "storage": LOCAL | CLOUD | BOTH
// (không gửi thì BE mặc định LOCAL, tương thích ngược).
export function upload(formData, onProgress) {
  return apiClient
    .post("/documents/upload", formData, { onUploadProgress: onProgress })
    .then((res) => res.data);
}

// Byte - dùng để chặn trước ở FE trước khi gửi file lớn/vượt quota lên server (xem
// UploadModal.jsx) và để hiển thị thanh dung lượng (My Documents, Hồ sơ). Luôn là số liệu
// của user đang đăng nhập (token) - không nhận userId từ client.
export function getStorageUsage() {
  return apiClient.get("/documents/storage-usage").then((res) => res.data);
}

// Whitelist đuôi tệp được phép tải lên (cấu hình bởi admin, chung mọi user). Form upload dùng để
// set thuộc tính accept + tiền-kiểm phía client. BE vẫn là chốt chặn cuối. { allowedExtensions }.
export function getAllowedFileTypes() {
  return apiClient.get("/documents/allowed-file-types").then((res) => res.data);
}

// Gọi API PUT /documents/{id} — sửa tiêu đề/mô tả/môn học.
export function updateDocument(id, data) {
  // data: { title?, description?, subjectIds? }  (field bỏ trống = giữ nguyên)
  return apiClient.put(`/documents/${id}`, data).then((res) => res.data);
}

// Gọi API POST /documents/{id}/favorite — bật/tắt yêu thích.
export function toggleFavorite(id) {
  return apiClient.post(`/documents/${id}/favorite`);
}
// Gọi API POST /documents/{id}/comment — thêm bình luận (kèm cờ khiếu nại nếu có).
export function addComment(id, content, { dispute = false, disputeNote } = {}) {
  return apiClient.post(`/documents/${id}/comment`, content, {
    headers: { "Content-Type": "text/plain; charset=UTF-8" },
    params: { dispute, ...(disputeNote ? { disputeNote } : {}) },
  });
}

// Gọi API PUT /documents/comments/{commentId} — sửa bình luận.
export function updateComment(commentId, content, { dispute = false, disputeNote } = {}) {
  return apiClient.put(`/documents/comments/${commentId}`, content, {
    headers: { "Content-Type": "text/plain; charset=UTF-8" },
    params: { dispute, ...(disputeNote ? { disputeNote } : {}) },
  });
}
// Gọi API DELETE /documents/comments/{commentId} — xoá bình luận.
export function deleteComment(commentId) {
  return apiClient.delete(`/documents/comments/${commentId}`);
}

// Gọi API POST /documents/{id}/rate — chấm điểm 1-5 sao.
export function rateDocument(id, star) {
  return apiClient.post(`/documents/${id}/rate`, null, { params: { star } });
}

// Gọi API DELETE /documents/{id} — xoá mềm (đưa vào thùng rác).
export function deleteDocument(id) {
  return apiClient.delete(`/documents/${id}`);
}

// Gọi API DELETE /documents/{id}/permanent — xoá vĩnh viễn khỏi thùng rác.
export function permanentDelete(id) {
  return apiClient.delete(`/documents/${id}/permanent`);
}

// Gọi API PUT /documents/{id}/restore — khôi phục tài liệu từ thùng rác.
export function restoreDocument(id) {
  return apiClient.put(`/documents/${id}/restore`);
}

// BE giờ trả về document sau khi đổi (visibility mới + moderationStatus + moderationReason)
// để FE hiển thị kết quả kiểm duyệt AI thay vì im lặng.
export function toggleVisibility(id) {
  return apiClient
    .put(`/documents/${id}/toggle-visibility`)
    .then((res) => res.data);
}

// Gọi API GET /documents/{id}/download — tải file gốc về (dạng blob).
export function downloadFile(id) {
  return apiClient.get(`/documents/${id}/download`, { responseType: "blob" });
}
