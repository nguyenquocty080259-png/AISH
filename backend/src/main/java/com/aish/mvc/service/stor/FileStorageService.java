package com.aish.mvc.service.stor;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeFile(MultipartFile file);

    /** Xóa file vật lý khỏi ổ đĩa (dùng khi xóa cứng). */
    void deleteFile(String fileName);
}