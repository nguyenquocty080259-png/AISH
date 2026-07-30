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

    // Whitelist đuôi tệp được phép tải lên (global, mọi user), lưu dạng chuỗi đuôi ngăn cách bằng
    // dấu phẩy - viết thường, không kèm dấu chấm. Default = đúng bộ loại tệp hệ thống vốn hỗ trợ
    // (tài liệu + ảnh phổ biến); trước đây KHÔNG có kiểm tra loại tệp nên đây là danh sách chuẩn
    // hoá "không regress". Chuỗi phải gọn trong 255 ký tự (giới hạn cột setting_value).
    public static final String UPLOAD_ALLOWED_EXTENSIONS_KEY = "UPLOAD_ALLOWED_EXTENSIONS";
    public static final String UPLOAD_ALLOWED_EXTENSIONS_DEFAULT =
            "pdf,doc,docx,ppt,pptx,xls,xlsx,txt,csv,png,jpg,jpeg,gif,webp";

    // AiChatService: số chunk lấy về (topK) khi truy vấn RAG.
    public static final String AI_TOP_K_KEY = "AI_TOP_K";
    public static final int AI_TOP_K_DEFAULT = 4;

    // AiChatService: ngưỡng similarity tối thiểu để chunk được coi là liên quan.
    public static final String AI_SIMILARITY_THRESHOLD_KEY = "AI_SIMILARITY_THRESHOLD";
    public static final double AI_SIMILARITY_THRESHOLD_DEFAULT = 0.55;

    // AiChatService: số tin nhắn gần đây đưa vào ngữ cảnh chat.
    public static final String AI_RECENT_MESSAGE_LIMIT_KEY = "AI_RECENT_MESSAGE_LIMIT";
    public static final int AI_RECENT_MESSAGE_LIMIT_DEFAULT = 10;

    // RecommendationServiceImpl: trọng số các tín hiệu chấm điểm gợi ý tài liệu.
    public static final String RECO_SUBJECT_OVERLAP_WEIGHT_KEY = "RECO_SUBJECT_OVERLAP_WEIGHT";
    public static final double RECO_SUBJECT_OVERLAP_WEIGHT_DEFAULT = 100.0;

    public static final String RECO_FAVORITE_WEIGHT_KEY = "RECO_FAVORITE_WEIGHT";
    public static final double RECO_FAVORITE_WEIGHT_DEFAULT = 3.0;

    public static final String RECO_DOWNLOAD_WEIGHT_KEY = "RECO_DOWNLOAD_WEIGHT";
    public static final double RECO_DOWNLOAD_WEIGHT_DEFAULT = 1.0;

    public static final String RECO_RATING_WEIGHT_KEY = "RECO_RATING_WEIGHT";
    public static final double RECO_RATING_WEIGHT_DEFAULT = 10.0;

    // DocEmbeddingServiceImpl: kích thước chunk (tokens) khi cắt tài liệu để embed.
    public static final String AI_CHUNK_SIZE_KEY = "AI_CHUNK_SIZE";
    public static final int AI_CHUNK_SIZE_DEFAULT = 800; // mặc định của TokenTextSplitter

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

    // Fail-safe giống hệt getInt()/getLong() - dùng cho các setting số thực (ngưỡng
    // similarity, trọng số gợi ý...).
    public double getDouble(String key, double defaultValue) {
        try {
            return systemSettingRepository.findBySettingKey(key)
                    .map(SystemSetting::getSettingValue)
                    .map(Double::parseDouble)
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

    // Fail-safe giống getInt()/getLong(): key vắng mặt, giá trị rỗng/trắng hoặc lỗi DB đều trả
    // về defaultValue thay vì ném exception - để cấu hình hỏng không làm sập luồng upload. Đọc
    // tươi từ DB mỗi lần gọi nên admin đổi giá trị là ăn liền, không cần restart.
    public String getString(String key, String defaultValue) {
        try {
            return systemSettingRepository.findBySettingKey(key)
                    .map(SystemSetting::getSettingValue)
                    .filter(value -> !value.isBlank())
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
