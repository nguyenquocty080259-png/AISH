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
        // Order(0) SchemaPatchRunner đã chạy trước, migrate MAX_UPLOAD_*_BYTES -> MAX_FILE_*_BYTES
        // nếu có - nên seedIfMissing dưới đây chỉ thực sự tạo mới trên DB hoàn toàn chưa có key nào.
        seedIfMissing(SystemSettingService.MIN_UPLOAD_AGE_KEY,
                String.valueOf(SystemSettingService.MIN_UPLOAD_AGE_DEFAULT));
        seedIfMissing(SystemSettingService.MAX_FILE_LOCAL_BYTES_KEY,
                String.valueOf(SystemSettingService.MAX_FILE_LOCAL_BYTES_DEFAULT));
        seedIfMissing(SystemSettingService.MAX_FILE_CLOUD_BYTES_KEY,
                String.valueOf(SystemSettingService.MAX_FILE_CLOUD_BYTES_DEFAULT));
        seedIfMissing(SystemSettingService.QUOTA_LOCAL_BYTES_KEY,
                String.valueOf(SystemSettingService.QUOTA_LOCAL_BYTES_DEFAULT));
        seedIfMissing(SystemSettingService.QUOTA_CLOUD_BYTES_KEY,
                String.valueOf(SystemSettingService.QUOTA_CLOUD_BYTES_DEFAULT));
        seedIfMissing(SystemSettingService.UPLOAD_ALLOWED_EXTENSIONS_KEY,
                SystemSettingService.UPLOAD_ALLOWED_EXTENSIONS_DEFAULT);
        seedIfMissing(SystemSettingService.AI_TOP_K_KEY,
                String.valueOf(SystemSettingService.AI_TOP_K_DEFAULT));
        seedIfMissing(SystemSettingService.AI_SIMILARITY_THRESHOLD_KEY,
                String.valueOf(SystemSettingService.AI_SIMILARITY_THRESHOLD_DEFAULT));
        seedIfMissing(SystemSettingService.AI_RECENT_MESSAGE_LIMIT_KEY,
                String.valueOf(SystemSettingService.AI_RECENT_MESSAGE_LIMIT_DEFAULT));
        seedIfMissing(SystemSettingService.RECO_SUBJECT_OVERLAP_WEIGHT_KEY,
                String.valueOf(SystemSettingService.RECO_SUBJECT_OVERLAP_WEIGHT_DEFAULT));
        seedIfMissing(SystemSettingService.RECO_FAVORITE_WEIGHT_KEY,
                String.valueOf(SystemSettingService.RECO_FAVORITE_WEIGHT_DEFAULT));
        seedIfMissing(SystemSettingService.RECO_DOWNLOAD_WEIGHT_KEY,
                String.valueOf(SystemSettingService.RECO_DOWNLOAD_WEIGHT_DEFAULT));
        seedIfMissing(SystemSettingService.RECO_RATING_WEIGHT_KEY,
                String.valueOf(SystemSettingService.RECO_RATING_WEIGHT_DEFAULT));
        seedIfMissing(SystemSettingService.AI_CHUNK_SIZE_KEY,
                String.valueOf(SystemSettingService.AI_CHUNK_SIZE_DEFAULT));
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
