package com.aish.mvc.service.doc.impl;

import com.aish.mvc.service.config.SystemSettingService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * enforceUploadSizeLimit() là gate mới thêm (T2, A2) - kiểm tra riêng cho từng storage
 * (LOCAL/CLOUD/BOTH) mà không cần dựng lại toàn bộ pipeline uploadDocument() (buildDocument,
 * file storage, Cloudinary...) vì gate ném exception TRƯỚC khi các bước đó chạy.
 */
class DocumentServiceImplUploadSizeLimitTest {

    private DocumentServiceImpl newServiceWithLimits(long maxLocal, long maxCloud) {
        SystemSettingService systemSettingService = mock(SystemSettingService.class);
        when(systemSettingService.getLong(eq(SystemSettingService.MAX_FILE_LOCAL_BYTES_KEY), eq(SystemSettingService.MAX_FILE_LOCAL_BYTES_DEFAULT)))
                .thenReturn(maxLocal);
        when(systemSettingService.getLong(eq(SystemSettingService.MAX_FILE_CLOUD_BYTES_KEY), eq(SystemSettingService.MAX_FILE_CLOUD_BYTES_DEFAULT)))
                .thenReturn(maxCloud);

        DocumentServiceImpl service = new DocumentServiceImpl();
        ReflectionTestUtils.setField(service, "systemSettingService", systemSettingService);
        return service;
    }

    @Test
    void localWithinLimitDoesNotThrow() {
        DocumentServiceImpl service = newServiceWithLimits(1000L, 1000L);
        assertDoesNotThrow(() -> service.enforceUploadSizeLimit(500L, "LOCAL"));
    }

    @Test
    void localOverLimitThrowsBadRequestNamingLocal() {
        DocumentServiceImpl service = newServiceWithLimits(1000L, 1_000_000_000L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.enforceUploadSizeLimit(1500L, "LOCAL"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(true, ex.getReason().contains("LOCAL"));
    }

    @Test
    void cloudOverLimitThrowsBadRequestNamingCloud() {
        DocumentServiceImpl service = newServiceWithLimits(1_000_000_000L, 1000L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.enforceUploadSizeLimit(1500L, "CLOUD"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(true, ex.getReason().contains("CLOUD"));
    }

    @Test
    void bothWithinBothLimitsDoesNotThrow() {
        DocumentServiceImpl service = newServiceWithLimits(1000L, 1000L);
        assertDoesNotThrow(() -> service.enforceUploadSizeLimit(900L, "BOTH"));
    }

    @Test
    void bothFailsWhenOnlyLocalLimitExceeded() {
        DocumentServiceImpl service = newServiceWithLimits(1000L, 1_000_000_000L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.enforceUploadSizeLimit(1500L, "BOTH"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(true, ex.getReason().contains("LOCAL"));
    }

    @Test
    void bothFailsWhenOnlyCloudLimitExceeded() {
        // Local giới hạn lớn (qua được), cloud giới hạn nhỏ (không qua được) -> phải báo lỗi CLOUD,
        // chứng minh BOTH thật sự kiểm tra CẢ HAI giới hạn chứ không dừng sớm sau khi LOCAL pass.
        DocumentServiceImpl service = newServiceWithLimits(1_000_000_000L, 1000L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.enforceUploadSizeLimit(1500L, "BOTH"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(true, ex.getReason().contains("CLOUD"));
    }
}
