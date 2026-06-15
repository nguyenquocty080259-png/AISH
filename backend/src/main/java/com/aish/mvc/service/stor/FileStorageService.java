package com.aish.mvc.service.stor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        try {
            // 1. Tạo thư mục nếu chưa có
            Path root = Paths.get(uploadDir);
            if (!Files.exists(root)) Files.createDirectories(root);

            // 2. Tạo tên file duy nhất để không bị trùng (dùng UUID)
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path targetPath = root.resolve(fileName);

            // 3. Copy file vào thư mục
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return fileName; // Trả về tên file để lưu vào DB
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file: " + e.getMessage());
        }
    }
}