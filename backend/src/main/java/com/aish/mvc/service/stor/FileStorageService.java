package com.aish.mvc.service.stor;

import org.springframework.web.multipart.MultipartFile;

/**
 * Lớp trừu tượng (interface) cho việc lưu trữ file trên server (ổ đĩa local).
 * Tuân thủ mô hình 3-layer: Controller -> Service (interface) -> ServiceImpl.
 */
public interface FileStorageService {

    /**
     * Lưu file vào thư mục cấu hình (app.upload.dir).
     *
     * @param file file người dùng gửi lên
     * @return tên file đã lưu (dùng để lưu vào DB)
     */
    String storeFile(MultipartFile file);
}