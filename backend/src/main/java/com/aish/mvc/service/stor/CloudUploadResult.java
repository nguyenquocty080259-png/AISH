package com.aish.mvc.service.stor;

/**
 * Kết quả trả về sau khi upload file lên cloud.
 * Dùng record (Java 21) thay cho Map<String,String> để có kiểu dữ liệu rõ ràng,
 * tránh truy cập bằng chuỗi "magic key" như uploaded.get("url").
 *
 * @param url          đường dẫn truy cập file (secure_url)
 * @param publicId     định danh public của file trên Cloudinary (dùng để xóa)
 * @param resourceType loại tài nguyên ("image" hoặc "raw")
 */
public record CloudUploadResult(
        String url,
        String publicId,
        String resourceType
) {
}