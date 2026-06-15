package com.aish.mvc.service.stor;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public Map<String, String> upload(MultipartFile file) {
        try {
            String contentType = file.getContentType() == null ? "" : file.getContentType();
            String resourceType = contentType.startsWith("image") ? "image" : "raw";

            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String publicId = UUID.randomUUID() + "_" + original;

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", resourceType,
                            "folder", "aish_study_hub",   // <-- upload vào đúng thư mục này
                            "asset_folder", "aish_study_hub",
                            "public_id", publicId,
                            "unique_filename", false
                    )
            );

            return Map.of(
                    "url", String.valueOf(result.get("secure_url")),
                    "publicId", String.valueOf(result.get("public_id")),
                    "resourceType", resourceType
            );
        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload Cloudinary: " + e.getMessage());
        }
    }

    public void delete(String publicId, String resourceType) {
        try {
            cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType == null ? "raw" : resourceType));
        } catch (IOException e) {
            throw new RuntimeException("Lỗi xóa file Cloudinary: " + e.getMessage());
        }
    }
}