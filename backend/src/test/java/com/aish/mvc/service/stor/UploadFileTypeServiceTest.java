package com.aish.mvc.service.stor;

import com.aish.mvc.service.config.SystemSettingService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Chốt chặn loại tệp: phải chặn theo CẢ đuôi (allowlist) LẪN content-type thật (Tika), để file
 * đổi đuôi giả không lọt. Allowlist rỗng -> fail-open (không chặn) để cấu hình hỏng không làm vỡ
 * luồng upload.
 */
class UploadFileTypeServiceTest {

    // %PDF header -> Tika dò ra application/pdf từ magic bytes.
    private static final byte[] PDF_BYTES = "%PDF-1.4\n1 0 obj<<>>endobj\n".getBytes();

    private UploadFileTypeService serviceWithAllowlist(String allowlist) {
        SystemSettingService settings = mock(SystemSettingService.class);
        when(settings.getString(eq(SystemSettingService.UPLOAD_ALLOWED_EXTENSIONS_KEY), any()))
                .thenReturn(allowlist);
        return new UploadFileTypeService(settings);
    }

    @Test
    void acceptsRealPdfWhenExtensionAllowed() {
        UploadFileTypeService service = serviceWithAllowlist("pdf,png,jpg");
        MockMultipartFile file = new MockMultipartFile("file", "bao-cao.pdf", "application/pdf", PDF_BYTES);

        assertDoesNotThrow(() -> service.validate(file));
    }

    @Test
    void rejectsExtensionNotInAllowlist() {
        UploadFileTypeService service = serviceWithAllowlist("pdf,png,jpg");
        MockMultipartFile file = new MockMultipartFile("file", "hack.exe", "application/octet-stream",
                new byte[]{0x4D, 0x5A, 0x00, 0x01});

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.validate(file));
        assertEqualsContains(ex, "không được phép");
    }

    @Test
    void rejectsSpoofedExtensionWhenContentTypeMismatch() {
        // Nội dung là text nhưng đặt tên .pdf -> đuôi hợp lệ, content-type thật (text/plain) không
        // khớp -> phải bị chặn (chống đổi đuôi lừa).
        UploadFileTypeService service = serviceWithAllowlist("pdf,png,jpg,txt");
        MockMultipartFile file = new MockMultipartFile("file", "fake.pdf", "application/pdf",
                "day chi la van ban thuong, khong phai PDF".getBytes());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.validate(file));
        assertEqualsContains(ex, "không khớp");
    }

    @Test
    void failsOpenWhenAllowlistEmpty() {
        UploadFileTypeService service = serviceWithAllowlist("");
        MockMultipartFile file = new MockMultipartFile("file", "anything.xyz", "application/octet-stream",
                new byte[]{1, 2, 3});

        assertDoesNotThrow(() -> service.validate(file));
    }

    private static void assertEqualsContains(ResponseStatusException ex, String needle) {
        String reason = ex.getReason() == null ? "" : ex.getReason();
        if (!reason.contains(needle)) {
            throw new AssertionError("Mong đợi thông báo chứa '" + needle + "' nhưng nhận: " + reason);
        }
    }
}
