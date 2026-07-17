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
        String key = SystemSettingService.MIN_UPLOAD_AGE_KEY;
        if (systemSettingRepository.existsBySettingKey(key)) {
            log.info("System setting {} already exists; skipping seed", key);
            return;
        }

        systemSettingRepository.save(SystemSetting.builder()
                .settingKey(key)
                .settingValue(String.valueOf(SystemSettingService.MIN_UPLOAD_AGE_DEFAULT))
                .build());
        log.info("Seeded system setting {}={}", key, SystemSettingService.MIN_UPLOAD_AGE_DEFAULT);
    }
}
