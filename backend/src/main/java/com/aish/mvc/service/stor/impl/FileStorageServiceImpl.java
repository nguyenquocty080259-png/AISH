package com.aish.mvc.service.stor.impl;

import com.aish.mvc.service.stor.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/**
 * Lưu file tài liệu NGAY TRÊN ĐĨA của máy chủ (nơi lưu "LOCAL"), khác với CloudinaryService là
 * lưu lên đám mây. Thư mục đích lấy từ cấu hình app.upload.dir (thường là thư mục "uploads").
 * Chỉ lo phần file vật lý — thông tin tài liệu (tiêu đề, ai up, môn học...) do DocumentServiceImpl
 * ghi xuống database.
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    // Đường dẫn thư mục chứa file upload, đọc từ application.properties (app.upload.dir).
    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Ghi file người dùng vừa tải lên xuống đĩa máy chủ.
     *
     * <p>Đầu vào: file người dùng gửi lên (MultipartFile). Trả về: TÊN FILE đã lưu trên đĩa
     * (không phải đường dẫn đầy đủ) — tên này được lưu vào cột file_url của bảng doc_files để
     * sau này đọc lại khi xem trước/tải về.
     *
     * <p>Các bước: (1) tạo thư mục uploads nếu chưa có, (2) đặt tên file mới không trùng,
     * (3) ghi nội dung file thật xuống đĩa.
     */
    @Override
    public String storeFile(MultipartFile file) {
        try {
            Path root = Paths.get(uploadDir);
            // B1: thư mục uploads chưa tồn tại (vd. lần chạy đầu tiên) thì tạo mới.
            if (!Files.exists(root)) Files.createDirectories(root);

            // B2: đổi tên thành "<mã ngẫu nhiên>_<tên gốc>" — hai người cùng up file tên
            // "baitap.pdf" sẽ ra hai tên khác nhau nên không ghi đè lên nhau. Vẫn giữ tên gốc
            // ở đuôi để nhìn vào thư mục còn đoán được đó là file gì.
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path targetPath = root.resolve(fileName);

            // B3: ghi nội dung file thật vào thư mục uploads trên server.
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            // Đưa cái file đã uplên sever vào file uploads

            return fileName;
        } catch (IOException e) {
            // Lỗi ghi đĩa (hết dung lượng, không có quyền ghi...) -> báo lỗi để dừng cả lần upload.
            throw new RuntimeException("Không thể lưu file: " + e.getMessage());
        }
    }

    /**
     * Xoá file vật lý khỏi thư mục uploads. Đầu vào là tên file đã lưu (giá trị mà
     * {@link #storeFile} trả về). Chỉ dùng khi XOÁ VĨNH VIỄN tài liệu — xoá vào thùng rác
     * không đụng tới file trên đĩa.
     */
    @Override
    public void deleteFile(String fileName) {
        try {
            // normalize() dọn các đoạn "../" trong tên để không xoá nhầm file ngoài thư mục uploads.
            Path path = Paths.get(uploadDir).resolve(fileName).normalize();
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // bỏ qua nếu file không tồn tại
        }
    }
}