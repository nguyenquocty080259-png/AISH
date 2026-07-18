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

    // Giới hạn MỘT file (đổi tên từ MAX_UPLOAD_*_BYTES - xem SchemaPatchRunner cho migration
    // các hàng cũ). Khác với QUOTA_*_BYTES bên dưới là tổng dung lượng của cả người dùng.
    public static final String MAX_FILE_LOCAL_BYTES_KEY = "MAX_FILE_LOCAL_BYTES";
    public static final long MAX_FILE_LOCAL_BYTES_DEFAULT = 209715200L; // 200 MiB

    public static final String MAX_FILE_CLOUD_BYTES_KEY = "MAX_FILE_CLOUD_BYTES";
    public static final long MAX_FILE_CLOUD_BYTES_DEFAULT = 209715200L; // 200 MiB

    // Tổng dung lượng tối đa của MỘT người dùng (áp dụng chung cho mọi user, không có override
    // theo từng người). Tính cả tài liệu trong thùng rác - xem DocFileRepository.
    public static final String QUOTA_LOCAL_BYTES_KEY = "QUOTA_LOCAL_BYTES";
    public static final long QUOTA_LOCAL_BYTES_DEFAULT = 1073741824L; // 1 GiB

    public static final String QUOTA_CLOUD_BYTES_KEY = "QUOTA_CLOUD_BYTES";
    public static final long QUOTA_CLOUD_BYTES_DEFAULT = 1073741824L; // 1 GiB

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

    // Fail-safe giống hệt getInt() ở trên, chỉ khác kiểu trả về - dùng cho các setting
    // lưu byte (MAX_FILE_*_BYTES, QUOTA_*_BYTES) vượt phạm vi int.
    public long getLong(String key, long defaultValue) {
        try {
            return systemSettingRepository.findBySettingKey(key)
                    .map(SystemSetting::getSettingValue)
                    .map(Long::parseLong)
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
