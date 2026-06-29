package com.aish.mvc.service.stor;

import org.springframework.web.multipart.MultipartFile;

/**
 * Lớp trừu tượng (interface) cho việc lưu trữ file trên cloud (Cloudinary).
 * Tuân thủ mô hình 3-layer: Controller -> Service (interface) -> ServiceImpl.
 */
public interface CloudinaryService {

    /**
     * Upload 1 file lên Cloudinary.
     *
     * @param file file người dùng gửi lên
     * @return thông tin file sau khi upload (url, publicId, resourceType)
     */
    CloudUploadResult upload(MultipartFile file);

    /**
     * Xóa file trên Cloudinary theo publicId.
     *
     * @param publicId     định danh public của file trên Cloudinary
     * @param resourceType loại tài nguyên ("image" hoặc "raw")
     */
    void delete(String publicId, String resourceType);
}