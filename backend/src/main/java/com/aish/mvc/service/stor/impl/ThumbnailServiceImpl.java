package com.aish.mvc.service.stor.impl;

import com.aish.mvc.service.stor.ThumbnailService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Sinh ảnh xem trước (thumbnail) cho tài liệu ngay lúc upload, để danh sách tài liệu ngoài FE
 * hiển thị được ảnh bìa thay vì icon trống. PDF thì vẽ lại trang đầu tiên, ảnh thì thu nhỏ chính
 * nó, các định dạng còn lại (docx/pptx/txt...) trả null.
 *
 * <p>Toàn bộ là "best-effort": thumbnail lỗi KHÔNG bao giờ làm hỏng lần upload — chỉ ghi log
 * cảnh báo rồi trả null.
 */
@Service
public class ThumbnailServiceImpl implements ThumbnailService {

    private static final Logger log = LoggerFactory.getLogger(ThumbnailServiceImpl.class);

    // Bề rộng thumbnail — đủ nét cho card ~300px kể cả màn retina, file vẫn nhẹ (~30-80KB).
    private static final int THUMB_WIDTH = 480;
    // DPI render PDF thấp có chủ đích: chỉ cần preview, không cần chất lượng in.
    private static final float PDF_RENDER_DPI = 72f;

    private static final String THUMB_SUBDIR = "thumbnails";

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Tạo ảnh thumbnail PNG cho file vừa upload.
     *
     * <p>Đầu vào: file người dùng tải lên. Trả về: đường dẫn tương đối của ảnh trong thư mục
     * uploads (vd. "thumbnails/abc.png") để lưu vào cột thumbnail_url của bảng doc_files;
     * trả null nếu định dạng không hỗ trợ hoặc vẽ ảnh thất bại.
     *
     * <p>Các bước: (1) xem file thuộc loại nào, (2) lấy ảnh gốc (PDF thì render trang 1, ảnh
     * thì đọc thẳng), (3) thu nhỏ về bề rộng chuẩn, (4) ghi ra file PNG trong uploads/thumbnails.
     */
    @Override
    public String createThumbnail(MultipartFile file) {
        try {
            String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
            String type = file.getContentType() == null ? "" : file.getContentType().toLowerCase();

            // B1+B2: chọn cách lấy ảnh gốc theo loại file.
            BufferedImage source;
            if (type.contains("pdf") || name.endsWith(".pdf")) {
                source = renderFirstPdfPage(file.getBytes()); // PDF: vẽ trang 1 thành ảnh
            } else if (type.startsWith("image/")) {
                source = ImageIO.read(new ByteArrayInputStream(file.getBytes())); // Ảnh: đọc thẳng
            } else {
                return null; // docx/pptx/txt... -> FE hiển thị icon, không cần thumbnail
            }

            if (source == null) return null;

            // B3+B4: thu nhỏ rồi ghi ra file PNG trên đĩa.
            BufferedImage scaled = scaleToWidth(source, THUMB_WIDTH);
            return savePng(scaled);
        }
        catch (Exception e) {
            // Best-effort: thumbnail hỏng không được phép làm fail upload.
            log.warn("Không tạo được thumbnail cho file '{}': {}", file.getOriginalFilename(), e.getMessage());
            return null;
        }
    }

    /**
     * Xoá file ảnh thumbnail khỏi đĩa. Đầu vào là đường dẫn tương đối đã lưu ở cột thumbnail_url.
     * Chỉ dùng khi XOÁ VĨNH VIỄN tài liệu. Không tìm thấy file thì bỏ qua, không báo lỗi.
     */
    @Override
    public void deleteThumbnail(String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) return;
        try {
            Path path = Paths.get(uploadDir).resolve(thumbnailUrl).normalize();
            // Chống path traversal: chỉ xóa file nằm TRONG uploads/thumbnails.
            if (path.startsWith(Paths.get(uploadDir).resolve(THUMB_SUBDIR).normalize())) {
                Files.deleteIfExists(path);
            }
        }
        catch (Exception ignored) {
        }
    }

    private BufferedImage renderFirstPdfPage(byte[] pdfBytes) throws Exception {
        // PDFBox 3.x: Loader.loadPDF. (Nếu project dùng PDFBox 2.x thì thay bằng PDDocument.load(pdfBytes))
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.getNumberOfPages() == 0) return null;
            return new PDFRenderer(document).renderImageWithDPI(0, PDF_RENDER_DPI);
        }
    }

    private BufferedImage scaleToWidth(BufferedImage src, int targetWidth) {
        if (src.getWidth() <= targetWidth) return flatten(src);
        int targetHeight = (int) Math.round(src.getHeight() * (targetWidth / (double) src.getWidth()));
        BufferedImage out = new BufferedImage(targetWidth, Math.max(1, targetHeight), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, targetWidth, targetHeight);
        g.drawImage(src, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return out;
    }

    // Ép về RGB nền trắng (PNG có alpha hoặc CMYK vẽ lên card sẽ ra nền đen xấu).
    private BufferedImage flatten(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_RGB) return src;
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, src.getWidth(), src.getHeight());
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return out;
    }

    private String savePng(BufferedImage image) throws Exception {
        Path dir = Paths.get(uploadDir).resolve(THUMB_SUBDIR);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        String fileName = UUID.randomUUID() + ".png";
        Path target = dir.resolve(fileName);
        ImageIO.write(image, "png", target.toFile());

        // Trả đường dẫn TƯƠNG ĐỐI so với uploadDir — FE ghép: http://host/uploads/<đường dẫn này>
        return THUMB_SUBDIR + "/" + fileName;
    }
}