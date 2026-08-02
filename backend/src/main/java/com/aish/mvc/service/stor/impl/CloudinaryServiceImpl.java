package com.aish.mvc.service.stor.impl;

import com.aish.mvc.service.stor.CloudUploadResult;
import com.aish.mvc.service.stor.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Lưu file tài liệu lên ĐÁM MÂY Cloudinary (nơi lưu "CLOUD"), thay vì ghi xuống đĩa máy chủ như
 * {@link com.aish.mvc.service.stor.FileStorageService}. File được đẩy lên thư mục "aish_study_hub"
 * trên Cloudinary; Cloudinary trả về đường dẫn (URL) và mã định danh publicId để sau này tải/xoá.
 */
@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    // Client Cloudinary đã cấu hình sẵn key/secret (xem lớp cấu hình Cloudinary trong config).
    @Autowired
    private Cloudinary cloudinary;

    /**
     * Đẩy file lên Cloudinary.
     *
     * <p>Đầu vào: file người dùng tải lên. Trả về {@link CloudUploadResult} gồm URL truy cập,
     * publicId (để xoá sau này) và loại tài nguyên.
     *
     * <p>Các bước: (1) xác định loại tài nguyên ảnh hay file thường, (2) đặt publicId không
     * trùng, (3) gọi API upload của Cloudinary, (4) gói kết quả trả về.
     */
    @Override
    public CloudUploadResult upload(MultipartFile file) {
        try {
            String contentType = file.getContentType() == null ? "" : file.getContentType();
            // B1: Cloudinary phân biệt "image" (xử lý được ảnh: resize, crop...) và "raw" (file
            // thường như PDF/DOCX chỉ lưu nguyên trạng). Chọn sai loại thì lúc tải về sẽ lỗi.
            String resourceType = contentType.startsWith("image") ? "image" : "raw";

            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            // B2: publicId = "<mã ngẫu nhiên>_<tên gốc>" — hai người up trùng tên file thì vẫn
            // ra hai publicId khác nhau, không đè lên nhau trên Cloudinary.
            String publicId = UUID.randomUUID() + "_" + original;

            // B3: gọi API Cloudinary, đẩy nội dung file lên thư mục "aish_study_hub".
            // unique_filename=false để Cloudinary dùng đúng publicId mình đặt ở trên.
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", resourceType,
                            "folder", "aish_study_hub",
                            "asset_folder", "aish_study_hub",
                            "public_id", publicId,
                            "unique_filename", false
                    )
            );

            // B4: lấy 3 thông tin cần lưu xuống bảng doc_files: đường dẫn https, publicId, loại.
            return new CloudUploadResult(
                    String.valueOf(result.get("secure_url")),
                    String.valueOf(result.get("public_id")),
                    resourceType
            );
        } catch (IOException e) {
            // Lỗi mạng / Cloudinary từ chối -> dừng cả lần upload.
            throw new RuntimeException("Lỗi upload Cloudinary: " + e.getMessage());
        }
    }

    // ===== METHOD MỚI =====
    /**
     * Sinh đường dẫn tải file có CHỮ KÝ (signed URL). Dùng khi URL công khai bị Cloudinary chặn —
     * tài khoản Cloudinary miễn phí mặc định không cho tải PDF qua link công khai (trả lỗi 401).
     *
     * <p>Đầu vào: publicId của file + loại tài nguyên. Trả về: một URL tạm dùng được để backend
     * tự đọc/tải file (xem {@link com.aish.mvc.service.stor.FileResourceResolver}).
     */
    @Override
    public String signedDownloadUrl(String publicId, String resourceType) {
        // fl_attachment: Cloudinary CHO PHÉP deliver PDF trên account free nếu tải dạng
        // attachment; signed(true) để URL hợp lệ cả với asset cấu hình authenticated.
        return cloudinary.url()
                .resourceType(resourceType == null ? "raw" : resourceType)
                .secure(true)
                .signed(true)
                .transformation(new com.cloudinary.Transformation().flags("attachment"))
                .generate(publicId);
    }

    /**
     * Xoá hẳn file khỏi Cloudinary theo publicId. Chỉ dùng khi XOÁ VĨNH VIỄN tài liệu —
     * xoá vào thùng rác không đụng tới file trên đám mây.
     */
    @Override
    public void delete(String publicId, String resourceType) {
        try {
            cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType == null ? "raw" : resourceType));
        } catch (IOException e) {
            throw new RuntimeException("Lỗi xóa file Cloudinary: " + e.getMessage());
        }
    }
}