package com.aish.mvc.service.stor;

import com.aish.mvc.service.config.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Chốt chặn loại tệp được phép tải lên (global, mọi user). Allowlist đuôi tệp lưu trong
 * system_settings ({@link SystemSettingService#UPLOAD_ALLOWED_EXTENSIONS_KEY}) và được đọc TƯƠI
 * mỗi lần upload, nên admin đổi danh sách là hiệu lực tức thì, không cần restart.
 *
 * <p>Chống đổi đuôi lừa: ngoài việc khớp đuôi tệp với allowlist, còn dùng Tika dò content-type
 * THẬT từ magic bytes (không tin phần mở rộng) và đối chiếu với đuôi. Bản đồ đuôi -> content-type
 * là hằng số trong code (không phải schema, không phải cấu hình admin) và chỉ dùng để phát hiện
 * giả mạo cho các loại "biết được"; đuôi nằm trong allowlist nhưng ngoài bản đồ này thì cho qua
 * theo đuôi (best-effort).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UploadFileTypeService {

    private final SystemSettingService systemSettingService;

    // Dùng lại 1 instance - Tika thread-safe cho detect().
    private final Tika tika = new Tika();

    // Đuôi -> tập content-type Tika chấp nhận khi dò theo NỘI DUNG (magic/container, bỏ qua tên
    // file). Gồm cả type container chung của Tika (x-tika-ooxml/x-tika-msoffice) làm lưới an toàn.
    private static final Map<String, Set<String>> EXTENSION_CONTENT_TYPES = Map.ofEntries(
            Map.entry("pdf", Set.of("application/pdf")),
            Map.entry("txt", Set.of("text/plain")),
            Map.entry("csv", Set.of("text/csv", "text/plain")),
            Map.entry("doc", Set.of("application/msword", "application/x-tika-msoffice")),
            Map.entry("xls", Set.of("application/vnd.ms-excel", "application/x-tika-msoffice")),
            Map.entry("ppt", Set.of("application/vnd.ms-powerpoint", "application/x-tika-msoffice")),
            Map.entry("docx", Set.of(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/x-tika-ooxml")),
            Map.entry("xlsx", Set.of(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/x-tika-ooxml")),
            Map.entry("pptx", Set.of(
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "application/x-tika-ooxml")),
            Map.entry("png", Set.of("image/png")),
            Map.entry("jpg", Set.of("image/jpeg")),
            Map.entry("jpeg", Set.of("image/jpeg")),
            Map.entry("gif", Set.of("image/gif")),
            Map.entry("webp", Set.of("image/webp"))
    );

    // Danh sách đuôi được phép hiện hành, chuẩn hoá (viết thường, bỏ trùng, giữ thứ tự). Rỗng
    // nếu cấu hình hỏng -> caller fail-open (xem validate()).
    public List<String> getAllowedExtensions() {
        String raw = systemSettingService.getString(
                SystemSettingService.UPLOAD_ALLOWED_EXTENSIONS_KEY,
                SystemSettingService.UPLOAD_ALLOWED_EXTENSIONS_DEFAULT);
        return parseExtensions(raw);
    }

    public static List<String> parseExtensions(String raw) {
        if (raw == null) return List.of();
        Set<String> result = new LinkedHashSet<>();
        for (String part : raw.split(",")) {
            String cleaned = part.trim().toLowerCase();
            if (cleaned.startsWith(".")) cleaned = cleaned.substring(1);
            if (!cleaned.isEmpty()) result.add(cleaned);
        }
        return new ArrayList<>(result);
    }

    /**
     * Ném {@link ResponseStatusException} 400 nếu tệp không hợp lệ. Kiểm tra CẢ HAI: (1) đuôi tệp
     * thuộc allowlist, (2) content-type thật do Tika dò khớp với đuôi. Allowlist rỗng (cấu hình
     * hỏng) -> fail-open, không chặn gì để không làm vỡ luồng upload.
     */
    public void validate(MultipartFile file) {
        List<String> allowed = getAllowedExtensions();
        if (allowed.isEmpty()) {
            log.warn("Allowlist loại tệp rỗng; bỏ qua kiểm tra loại tệp (fail-open).");
            return;
        }

        String filename = file.getOriginalFilename();
        String ext = extensionOf(filename);
        if (ext.isEmpty() || !allowed.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Loại tệp" + (ext.isEmpty() ? "" : " ." + ext) + " không được phép tải lên. "
                            + "Các loại được phép: " + String.join(", ", allowed) + ".");
        }

        // Đối chiếu nội dung thật (chống đổi đuôi) - chỉ với các loại có trong bản đồ.
        Set<String> expected = EXTENSION_CONTENT_TYPES.get(ext);
        if (expected == null) return;

        String detected;
        try (InputStream is = file.getInputStream()) {
            // Dò theo nội dung, KHÔNG truyền tên file để phần mở rộng không "gợi ý" sai cho Tika.
            detected = tika.detect(is);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Không đọc được nội dung tệp để kiểm tra định dạng.");
        }
        String base = detected == null ? "" : detected.split(";")[0].trim().toLowerCase();
        if (!expected.contains(base)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nội dung tệp (" + base + ") không khớp với đuôi ." + ext
                            + " — nghi ngờ đổi đuôi. Vui lòng tải lên tệp đúng định dạng.");
        }
    }

    private static String extensionOf(String filename) {
        if (filename == null) return "";
        String name = filename.trim().toLowerCase();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return "";
        return name.substring(dot + 1);
    }
}
