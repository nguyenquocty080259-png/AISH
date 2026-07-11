package com.aish.mvc.service.stor;

import com.aish.mvc.entity.doc.DocFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Chuyển 1 DocFile thành Resource đọc được — dùng CHUNG cho download/preview
 * (DocumentController) và ingest AI (DocEmbeddingServiceImpl), thay cho 2 bản
 * resolveResource() copy-paste trước đây.
 *
 * Xử lý luôn ca Cloudinary chặn deliver PDF/raw trên account free (trả 401):
 * nếu URL public không đọc được và file có publicId thì fallback sang signed URL
 * kèm fl_attachment — cách Cloudinary chính thức cho phép tải PDF trên account
 * chưa được "trusted".
 */
@Component
public class FileResourceResolver {

    private static final Logger log = LoggerFactory.getLogger(FileResourceResolver.class);

    private final CloudinaryService cloudinaryService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public FileResourceResolver(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    public Resource resolve(DocFile docFile) {
        String fileUrl = docFile.getFileUrl();

        // File local: fileUrl là tên file trong thư mục uploads.
        if (fileUrl == null || !fileUrl.startsWith("http")) {
            try {
                Path filePath = Paths.get(uploadDir).resolve(fileUrl == null ? "" : fileUrl).normalize();
                return new UrlResource(filePath.toUri());
            }
            catch (Exception e) {
                throw new IllegalStateException("Không đọc được file local: " + e.getMessage(), e);
            }
        }

        // File cloud: thử URL public trước.
        Resource direct = tryUrl(fileUrl);
        if (direct != null) return direct;

        // Public URL fail (thường là Cloudinary chặn PDF trên account free -> 401)
        // -> fallback signed URL + fl_attachment.
        if (docFile.getPublicId() != null) {
            String signed = cloudinaryService.signedDownloadUrl(docFile.getPublicId(), docFile.getResourceType());
            log.info("URL public Cloudinary không đọc được, chuyển sang signed URL cho publicId={}", docFile.getPublicId());
            Resource fallback = tryUrl(signed);
            if (fallback != null) return fallback;
        }

        throw new IllegalStateException(
                "Không đọc được file trên Cloudinary (URL trả lỗi — nếu là PDF, kiểm tra "
                        + "Settings > Security > 'Allow delivery of PDF and ZIP files' trên Cloudinary).");
    }

    private Resource tryUrl(String url) {
        try {
            UrlResource resource = new UrlResource(new URL(url));
            // exists() với http sẽ mở kết nối thật — 401/404 trả false thay vì ném lỗi lúc đọc.
            return resource.exists() ? resource : null;
        }
        catch (Exception e) {
            log.warn("Không mở được URL '{}': {}", url, e.getMessage());
            return null;
        }
    }
}