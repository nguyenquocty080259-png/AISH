package com.aish.mvc.service.stor;

import org.springframework.web.multipart.MultipartFile;

/**
 * Sinh ảnh thumbnail (PNG) cho tài liệu lúc upload.
 * - PDF  -> render trang 1 bằng PDFBox.
 * - Ảnh  -> thu nhỏ chính ảnh đó.
 * - Khác -> null (FE tự fallback về icon).
 *
 * Best-effort: KHÔNG bao giờ ném exception ra ngoài — thumbnail lỗi thì trả null,
 * upload vẫn thành công bình thường.
 */
public interface ThumbnailService {

    /**
     * @return đường dẫn tương đối trong thư mục upload (vd. "thumbnails/xxx.png"),
     *         hoặc null nếu định dạng không hỗ trợ / render lỗi.
     */
    String createThumbnail(MultipartFile file);

    /** Xóa file thumbnail vật lý (dùng khi xóa cứng tài liệu). Bỏ qua nếu không tồn tại. */
    void deleteThumbnail(String thumbnailUrl);
}