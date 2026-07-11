package com.aish.mvc.service.stor;

import org.springframework.web.multipart.MultipartFile;

/**
 * Lớp trừu tượng (interface) cho việc lưu trữ file trên cloud (Cloudinary).
 * Tuân thủ mô hình 3-layer: Controller -> Service (interface) -> ServiceImpl.
 */
public interface CloudinaryService {

    CloudUploadResult upload(MultipartFile file);

    void delete(String publicId, String resourceType);

    /**
     * Sinh signed URL kèm cờ fl_attachment để backend tự đọc file (ingest AI,
     * download/preview) kể cả khi Cloudinary chặn deliver public (vd. PDF trên
     * account free bị 401).
     */
    String signedDownloadUrl(String publicId, String resourceType);
}