// Chỉ còn xử lý URL tuyệt đối (Cloudinary). File local giờ đi qua endpoint có xác thực
// (/documents/{id}/preview, /documents/{id}/thumbnail) — xem documentApi.js + DocumentThumb.jsx.
export function uploadUrl(raw) {
  if (!raw) return null;
  return raw.startsWith("http") ? raw : null;
}

// Ảnh thumbnail do BE sinh lúc upload (thumbnailUrl trong DTO). Chỉ còn dùng cho trường hợp
// Cloudinary (URL tuyệt đối) — thumbnail local phải gọi documentApi.getThumbnail(id).
export function thumbnailUrl(doc) {
  return doc?.thumbnailUrl ? uploadUrl(doc.thumbnailUrl) : null;
}
