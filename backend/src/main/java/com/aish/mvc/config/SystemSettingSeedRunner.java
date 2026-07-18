package com.aish.mvc.config;

import com.aish.mvc.entity.config.SystemSetting;
import com.aish.mvc.repository.config.SystemSettingRepository;
import com.aish.mvc.service.config.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class SystemSettingSeedRunner implements CommandLineRunner {

    private final SystemSettingRepository systemSettingRepository;

    @Override
    public void run(String... args) {
        seedIfMissing(SystemSettingService.MIN_UPLOAD_AGE_KEY,
                String.valueOf(SystemSettingService.MIN_UPLOAD_AGE_DEFAULT));
        seedIfMissing(SystemSettingService.MAX_UPLOAD_LOCAL_BYTES_KEY,
                String.valueOf(SystemSettingService.MAX_UPLOAD_LOCAL_BYTES_DEFAULT));
        seedIfMissing(SystemSettingService.MAX_UPLOAD_CLOUD_BYTES_KEY,
                String.valueOf(SystemSettingService.MAX_UPLOAD_CLOUD_BYTES_DEFAULT));
    }

    // Mỗi key seed độc lập - key đã tồn tại (kể cả admin đã sửa giá trị) thì bỏ qua riêng
    // key đó, không skip toàn bộ chỉ vì 1 key khác đã có sẵn.
    private void seedIfMissing(String key, String defaultValue) {
        if (systemSettingRepository.existsBySettingKey(key)) {
            log.info("System setting {} already exists; skipping seed", key);
            return;
        }

        systemSettingRepository.save(SystemSetting.builder()
                .settingKey(key)
                .settingValue(defaultValue)
                .build());
        log.info("Seeded system setting {}={}", key, defaultValue);
    }
}
