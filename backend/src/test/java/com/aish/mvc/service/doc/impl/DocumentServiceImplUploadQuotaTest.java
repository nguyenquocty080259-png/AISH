package com.aish.mvc.service.doc.impl;

import com.aish.mvc.repository.doc.DocFileRepository;
import com.aish.mvc.service.config.SystemSettingService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * enforceUploadQuota() (A2b, T4) - kiểm tra dung lượng ĐÃ DÙNG + file mới có vượt quota tổng
 * của user hay không, tách theo LOCAL/CLOUD, BOTH phải kiểm tra độc lập cả 2 phía (không dừng
 * sớm). usedLocal/usedCloud ở đây mock trực tiếp DocFileRepository - hành vi COALESCE(SUM,0) và
 * "thùng rác vẫn tính" được xác nhận riêng ở DocFileRepositoryTest (test tầng repository thật).
 */
class DocumentServiceImplUploadQuotaTest {

    private static final Long USER_ID = 42L;

    private DocumentServiceImpl newService(long usedLocal, long usedCloud, long quotaLocal, long quotaCloud) {
        DocFileRepository docFileRepository = mock(DocFileRepository.class);
        when(docFileRepository.sumLocalFileSizeByUserId(USER_ID)).thenReturn(usedLocal);
        when(docFileRepository.sumCloudFileSizeByUserId(USER_ID)).thenReturn(usedCloud);

        SystemSettingService systemSettingService = mock(SystemSettingService.class);
        when(systemSettingService.getLong(eq(SystemSettingService.QUOTA_LOCAL_BYTES_KEY), anyLong()))
                .thenReturn(quotaLocal);
        when(systemSettingService.getLong(eq(SystemSettingService.QUOTA_CLOUD_BYTES_KEY), anyLong()))
                .thenReturn(quotaCloud);

        DocumentServiceImpl service = new DocumentServiceImpl();
        ReflectionTestUtils.setField(service, "docFileRepository", docFileRepository);
        ReflectionTestUtils.setField(service, "systemSettingService", systemSettingService);
        return service;
    }

    @Test
    void localWithinRemainingQuotaDoesNotThrow() {
        DocumentServiceImpl service = newService(500L, 0L, 1000L, 1000L);
        // used 500 + new 400 = 900 <= quota 1000
        assertDoesNotThrow(() -> service.enforceUploadQuota(400L, "LOCAL", USER_ID));
    }

    @Test
    void localExceedingRemainingQuotaThrowsNamingLocal() {
        DocumentServiceImpl service = newService(900L, 0L, 1000L, 1_000_000_000L);
        // used 900 + new 200 = 1100 > quota 1000
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.enforceUploadQuota(200L, "LOCAL", USER_ID));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(true, ex.getReason().contains("LOCAL"));
    }

    @Test
    void cloudExceedingRemainingQuotaThrowsNamingCloud() {
        DocumentServiceImpl service = newService(0L, 900L, 1_000_000_000L, 1000L);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.enforceUploadQuota(200L, "CLOUD", USER_ID));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(true, ex.getReason().contains("CLOUD"));
    }

    @Test
    void bothWithinBothQuotasDoesNotThrow() {
        DocumentServiceImpl service = newService(0L, 0L, 1000L, 1000L);
        assertDoesNotThrow(() -> service.enforceUploadQuota(900L, "BOTH", USER_ID));
    }

    @Test
    void bothFailsWhenOnlyLocalQuotaExceeded() {
        DocumentServiceImpl service = newService(900L, 0L, 1000L, 1_000_000_000L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.enforceUploadQuota(200L, "BOTH", USER_ID));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(true, ex.getReason().contains("LOCAL"));
    }

    @Test
    void bothFailsWhenOnlyCloudQuotaExceeded() {
        // LOCAL đủ chỗ (qua được), CLOUD không đủ (không qua được) -> phải báo lỗi CLOUD,
        // chứng minh BOTH thật sự kiểm tra CẢ HAI quota chứ không dừng sớm sau khi LOCAL pass.
        DocumentServiceImpl service = newService(0L, 900L, 1_000_000_000L, 1000L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.enforceUploadQuota(200L, "BOTH", USER_ID));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(true, ex.getReason().contains("CLOUD"));
    }
}
