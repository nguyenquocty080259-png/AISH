package com.aish.mvc.service.config;

import com.aish.mvc.entity.config.SystemSetting;
import com.aish.mvc.repository.config.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSettingService {

    public static final String MIN_UPLOAD_AGE_KEY = "MIN_UPLOAD_AGE";
    public static final int MIN_UPLOAD_AGE_DEFAULT = 16;

    private final SystemSettingRepository systemSettingRepository;

    // Fail-safe: bất kỳ lỗi nào (không tìm thấy key, giá trị không parse được số, lỗi DB...)
    // đều trả về defaultValue thay vì ném exception, để cấu hình hỏng không làm sập luồng chính.
    public int getInt(String key, int defaultValue) {
        try {
            return systemSettingRepository.findBySettingKey(key)
                    .map(SystemSetting::getSettingValue)
                    .map(Integer::parseInt)
                    .orElse(defaultValue);
        } catch (Exception exception) {
            log.warn("Không đọc được system setting {}; dùng giá trị mặc định {}: {}",
                    key, defaultValue, exception.getMessage());
            return defaultValue;
        }
    }

    @Transactional
    public void setValue(String key, String value) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElseGet(() -> SystemSetting.builder().settingKey(key).build());
        setting.setSettingValue(value);
        systemSettingRepository.save(setting);
    }
}
