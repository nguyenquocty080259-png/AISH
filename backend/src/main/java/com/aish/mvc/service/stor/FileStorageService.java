package com.aish.mvc.service.stor;

import org.springframework.web.multipart.MultipartFile;

/**
 * Lớp trừu tượng (interface) cho việc lưu file tài liệu trên ĐĨA máy chủ — nơi lưu "LOCAL".
 * Cặp đôi với {@link CloudinaryService} (lưu trên đám mây). Tuân thủ mô hình 3 lớp:
 * Controller -> Service (interface) -> ServiceImpl.
 */
public interface FileStorageService {

    /**
     * Ghi file người dùng tải lên xuống thư mục uploads.
     *
     * @return tên file đã lưu trên đĩa (đã đổi tên để không trùng); giá trị này được lưu vào
     *         cột file_url của bảng doc_files để lần sau đọc lại file
     */
    String storeFile(MultipartFile file);

    /** Xóa file vật lý khỏi ổ đĩa (dùng khi xóa cứng). */
    void deleteFile(String fileName);
}