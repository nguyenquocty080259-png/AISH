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

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public CloudUploadResult upload(MultipartFile file) {
        try {
            String contentType = file.getContentType() == null ? "" : file.getContentType();
            String resourceType = contentType.startsWith("image") ? "image" : "raw";

            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String publicId = UUID.randomUUID() + "_" + original;

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

            return new CloudUploadResult(
                    String.valueOf(result.get("secure_url")),
                    String.valueOf(result.get("public_id")),
                    resourceType
            );
        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload Cloudinary: " + e.getMessage());
        }
    }

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
