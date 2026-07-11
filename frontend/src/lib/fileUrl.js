// Ghép URL file tĩnh phục vụ từ backend (thư mục /uploads).
// - raw bắt đầu bằng "http" -> URL tuyệt đối (Cloudinary), giữ nguyên.
// - còn lại -> tên/đường dẫn tương đối trong /uploads.
const FILE_HOST = "http://localhost:8080";

export function uploadUrl(raw) {
  if (!raw) return null;
  return raw.startsWith("http") ? raw : `${FILE_HOST}/uploads/${raw}`;
}

// Ảnh thumbnail do BE sinh lúc upload (thumbnailUrl trong DTO, vd. "thumbnails/x.png").
// Trả null nếu không có -> caller tự fallback (iframe PDF / icon).
export function thumbnailUrl(doc) {
  return doc?.thumbnailUrl ? uploadUrl(doc.thumbnailUrl) : null;
}