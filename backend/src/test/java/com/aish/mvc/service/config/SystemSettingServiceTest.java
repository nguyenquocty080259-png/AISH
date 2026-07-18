package com.aish.mvc.service.config;

import com.aish.mvc.entity.config.SystemSetting;
import com.aish.mvc.repository.config.SystemSettingRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * getLong() phải fail-safe giống hệt getInt(): mọi lỗi (key vắng mặt, không parse được số,
 * lỗi repository) đều trả về defaultValue thay vì ném exception.
 */
class SystemSettingServiceTest {

    @Test
    void getLongReturnsStoredValueWhenPresentAndValid() {
        SystemSettingRepository repository = mock(SystemSettingRepository.class);
        when(repository.findBySettingKey("MAX_UPLOAD_LOCAL_BYTES"))
                .thenReturn(Optional.of(SystemSetting.builder().settingValue("2147483648").build()));

        SystemSettingService service = new SystemSettingService(repository);

        assertEquals(2147483648L, service.getLong("MAX_UPLOAD_LOCAL_BYTES", 999L));
    }

    @Test
    void getLongReturnsDefaultWhenKeyMissing() {
        SystemSettingRepository repository = mock(SystemSettingRepository.class);
        when(repository.findBySettingKey("MISSING_KEY")).thenReturn(Optional.empty());

        SystemSettingService service = new SystemSettingService(repository);

        assertEquals(1073741824L, service.getLong("MISSING_KEY", 1073741824L));
    }

    @Test
    void getLongReturnsDefaultWhenValueIsNotParseable() {
        SystemSettingRepository repository = mock(SystemSettingRepository.class);
        when(repository.findBySettingKey("MAX_UPLOAD_CLOUD_BYTES"))
                .thenReturn(Optional.of(SystemSetting.builder().settingValue("not-a-number").build()));

        SystemSettingService service = new SystemSettingService(repository);

        assertEquals(555L, service.getLong("MAX_UPLOAD_CLOUD_BYTES", 555L));
    }

    @Test
    void getLongReturnsDefaultWhenRepositoryThrows() {
        SystemSettingRepository repository = mock(SystemSettingRepository.class);
        when(repository.findBySettingKey("ANY_KEY")).thenThrow(new RuntimeException("db down"));

        SystemSettingService service = new SystemSettingService(repository);

        assertEquals(42L, service.getLong("ANY_KEY", 42L));
    }
}
