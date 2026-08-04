package com.aish.mvc.controller.admin;

import com.aish.mvc.dto.config.AiConfigDTO;
import com.aish.mvc.dto.config.MinUploadAgeDTO;
import com.aish.mvc.dto.config.UploadFileTypesDTO;
import com.aish.mvc.dto.config.UploadLimitsDTO;
import com.aish.mvc.service.config.SystemSettingService;
import com.aish.mvc.service.stor.UploadFileTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * TRANG CẤU HÌNH HỆ THỐNG cho Admin: tuổi tối thiểu được upload, giới hạn dung lượng/quota, loại
 * tệp cho phép, và tham số AI (topK, ngưỡng tương đồng, trọng số gợi ý, kích thước chunk). Mọi
 * giá trị đọc/ghi qua {@link SystemSettingService} (lưu database, có hiệu lực ngay không cần khởi
 * động lại server). Mỗi API cập nhật đều validate khoảng giá trị hợp lý trước khi lưu.
 */
@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class SystemSettingAdminController {

    // Trần cứng của spring.servlet.multipart.max-file-size (application.properties) - giới
    // hạn admin cấu hình không được vượt quá trần này, nếu không request sẽ bị Tomcat/Spring
    // chặn ở tầng multipart trước khi tới được gate của DocumentServiceImpl.
    private static final long MULTIPART_CEILING_BYTES = 2L * 1024 * 1024 * 1024; // 2 GiB

    // Trần độ dài chuỗi allowlist khi ghép lại - khớp giới hạn cột setting_value (VARCHAR 255).
    private static final int ALLOWED_EXTENSIONS_MAX_LENGTH = 255;

    private final SystemSettingService systemSettingService;
    private final UploadFileTypeService uploadFileTypeService;

    // ===== Tuổi tối thiểu được phép tải tài liệu lên =====
    @GetMapping("/min-upload-age")
    public ResponseEntity<MinUploadAgeDTO> getMinUploadAge() {
        int value = systemSettingService.getInt(
                SystemSettingService.MIN_UPLOAD_AGE_KEY, SystemSettingService.MIN_UPLOAD_AGE_DEFAULT);
        return ResponseEntity.ok(new MinUploadAgeDTO(value));
    }

    @PutMapping("/min-upload-age")
    public ResponseEntity<MinUploadAgeDTO> updateMinUploadAge(@RequestBody MinUploadAgeDTO request) {
        Integer value = request.getMinUploadAge();
        if (value == null || value < 6 || value > 100) {
            throw new IllegalArgumentException("error.settings.minAgeRange");
        }
        systemSettingService.setValue(SystemSettingService.MIN_UPLOAD_AGE_KEY, String.valueOf(value));
        return ResponseEntity.ok(new MinUploadAgeDTO(value));
    }

    // ===== Giới hạn dung lượng file/quota (LOCAL và CLOUD) =====
    @GetMapping("/upload-limits")
    public ResponseEntity<UploadLimitsDTO> getUploadLimits() {
        return ResponseEntity.ok(readUploadLimits());
    }

    @PutMapping("/upload-limits")
    public ResponseEntity<UploadLimitsDTO> updateUploadLimits(@RequestBody UploadLimitsDTO request) {
        Long maxFileLocal = request.getMaxFileLocalBytes();
        Long maxFileCloud = request.getMaxFileCloudBytes();
        Long quotaLocal = request.getQuotaLocalBytes();
        Long quotaCloud = request.getQuotaCloudBytes();

        validateUploadLimit(maxFileLocal, "tệp LOCAL");
        validateUploadLimit(maxFileCloud, "tệp CLOUD");
        validateUploadLimit(quotaLocal, "quota LOCAL");
        validateUploadLimit(quotaCloud, "quota CLOUD");

        // Giới hạn 1 file lớn hơn quota tổng là vô lý — không file nào tải lên được nữa.
        if (maxFileLocal > quotaLocal) {
            throw new IllegalArgumentException(
                    "Giới hạn dung lượng tệp LOCAL không được vượt quá quota LOCAL.");
        }
        if (maxFileCloud > quotaCloud) {
            throw new IllegalArgumentException(
                    "Giới hạn dung lượng tệp CLOUD không được vượt quá quota CLOUD.");
        }

        systemSettingService.setValue(SystemSettingService.MAX_FILE_LOCAL_BYTES_KEY, String.valueOf(maxFileLocal));
        systemSettingService.setValue(SystemSettingService.MAX_FILE_CLOUD_BYTES_KEY, String.valueOf(maxFileCloud));
        systemSettingService.setValue(SystemSettingService.QUOTA_LOCAL_BYTES_KEY, String.valueOf(quotaLocal));
        systemSettingService.setValue(SystemSettingService.QUOTA_CLOUD_BYTES_KEY, String.valueOf(quotaCloud));
        return ResponseEntity.ok(readUploadLimits());
    }

    // ===== Danh sách đuôi tệp được phép upload =====
    @GetMapping("/upload-file-types")
    public ResponseEntity<UploadFileTypesDTO> getUploadFileTypes() {
        return ResponseEntity.ok(new UploadFileTypesDTO(uploadFileTypeService.getAllowedExtensions()));
    }

    @PutMapping("/upload-file-types")
    public ResponseEntity<UploadFileTypesDTO> updateUploadFileTypes(@RequestBody UploadFileTypesDTO request) {
        // Chuẩn hoá y hệt lúc đọc (viết thường, bỏ dấu chấm, bỏ trùng, giữ thứ tự) để lưu và trả
        // về nhất quán. Nhận cả khi client gửi 1 chuỗi có phẩy trong 1 phần tử.
        List<String> normalized = UploadFileTypeService.parseExtensions(
                request.getAllowedExtensions() == null ? "" : String.join(",", request.getAllowedExtensions()));

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("error.settings.fileTypesRequired");
        }
        for (String ext : normalized) {
            if (!ext.matches("[a-z0-9]{1,12}")) {
                throw new IllegalArgumentException(
                        "Đuôi tệp '" + ext + "' không hợp lệ - chỉ gồm chữ thường/số, tối đa 12 ký tự.");
            }
        }
        String joined = String.join(",", normalized);
        if (joined.length() > ALLOWED_EXTENSIONS_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Danh sách loại tệp quá dài (tối đa " + ALLOWED_EXTENSIONS_MAX_LENGTH + " ký tự khi ghép lại).");
        }

        systemSettingService.setValue(SystemSettingService.UPLOAD_ALLOWED_EXTENSIONS_KEY, joined);
        return ResponseEntity.ok(new UploadFileTypesDTO(normalized));
    }

    // ===== Tham số AI: topK, ngưỡng tương đồng, số tin nhắn nhớ gần nhất, trọng số gợi ý, kích thước chunk =====
    @GetMapping("/ai-config")
    public ResponseEntity<AiConfigDTO> getAiConfig() {
        return ResponseEntity.ok(readAiConfig());
    }

    @PutMapping("/ai-config")
    public ResponseEntity<AiConfigDTO> updateAiConfig(@RequestBody AiConfigDTO request) {
        Integer topK = request.getTopK();
        Double similarityThreshold = request.getSimilarityThreshold();
        Integer recentMessageLimit = request.getRecentMessageLimit();
        Double subjectOverlapWeight = request.getSubjectOverlapWeight();
        Double favoriteWeight = request.getFavoriteWeight();
        Double downloadWeight = request.getDownloadWeight();
        Double ratingWeight = request.getRatingWeight();
        Integer chunkSize = request.getChunkSize();

        if (topK == null || topK < 1 || topK > 20) {
            throw new IllegalArgumentException("error.settings.aiTopKRange");
        }
        if (similarityThreshold == null || similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("error.settings.aiThresholdRange");
        }
        if (recentMessageLimit == null || recentMessageLimit < 0 || recentMessageLimit > 50) {
            throw new IllegalArgumentException("error.settings.aiRecentLimitRange");
        }
        validateNonNegativeWeight(subjectOverlapWeight);
        validateNonNegativeWeight(favoriteWeight);
        validateNonNegativeWeight(downloadWeight);
        validateNonNegativeWeight(ratingWeight);
        if (chunkSize == null || chunkSize < 100 || chunkSize > 2000) {
            throw new IllegalArgumentException("error.settings.aiChunkSizeRange");
        }

        systemSettingService.setValue(SystemSettingService.AI_TOP_K_KEY, String.valueOf(topK));
        systemSettingService.setValue(
                SystemSettingService.AI_SIMILARITY_THRESHOLD_KEY, String.valueOf(similarityThreshold));
        systemSettingService.setValue(
                SystemSettingService.AI_RECENT_MESSAGE_LIMIT_KEY, String.valueOf(recentMessageLimit));
        systemSettingService.setValue(
                SystemSettingService.RECO_SUBJECT_OVERLAP_WEIGHT_KEY, String.valueOf(subjectOverlapWeight));
        systemSettingService.setValue(SystemSettingService.RECO_FAVORITE_WEIGHT_KEY, String.valueOf(favoriteWeight));
        systemSettingService.setValue(SystemSettingService.RECO_DOWNLOAD_WEIGHT_KEY, String.valueOf(downloadWeight));
        systemSettingService.setValue(SystemSettingService.RECO_RATING_WEIGHT_KEY, String.valueOf(ratingWeight));
        systemSettingService.setValue(SystemSettingService.AI_CHUNK_SIZE_KEY, String.valueOf(chunkSize));
        return ResponseEntity.ok(readAiConfig());
    }

    // Đọc toàn bộ tham số AI hiện tại từ database (hoặc giá trị mặc định nếu chưa cấu hình).
    private AiConfigDTO readAiConfig() {
        int topK = systemSettingService.getInt(
                SystemSettingService.AI_TOP_K_KEY, SystemSettingService.AI_TOP_K_DEFAULT);
        double similarityThreshold = systemSettingService.getDouble(
                SystemSettingService.AI_SIMILARITY_THRESHOLD_KEY,
                SystemSettingService.AI_SIMILARITY_THRESHOLD_DEFAULT);
        int recentMessageLimit = systemSettingService.getInt(
                SystemSettingService.AI_RECENT_MESSAGE_LIMIT_KEY,
                SystemSettingService.AI_RECENT_MESSAGE_LIMIT_DEFAULT);
        double subjectOverlapWeight = systemSettingService.getDouble(
                SystemSettingService.RECO_SUBJECT_OVERLAP_WEIGHT_KEY,
                SystemSettingService.RECO_SUBJECT_OVERLAP_WEIGHT_DEFAULT);
        double favoriteWeight = systemSettingService.getDouble(
                SystemSettingService.RECO_FAVORITE_WEIGHT_KEY, SystemSettingService.RECO_FAVORITE_WEIGHT_DEFAULT);
        double downloadWeight = systemSettingService.getDouble(
                SystemSettingService.RECO_DOWNLOAD_WEIGHT_KEY, SystemSettingService.RECO_DOWNLOAD_WEIGHT_DEFAULT);
        double ratingWeight = systemSettingService.getDouble(
                SystemSettingService.RECO_RATING_WEIGHT_KEY, SystemSettingService.RECO_RATING_WEIGHT_DEFAULT);
        int chunkSize = systemSettingService.getInt(
                SystemSettingService.AI_CHUNK_SIZE_KEY, SystemSettingService.AI_CHUNK_SIZE_DEFAULT);
        return new AiConfigDTO(topK, similarityThreshold, recentMessageLimit, subjectOverlapWeight,
                favoriteWeight, downloadWeight, ratingWeight, chunkSize);
    }

    // Trọng số gợi ý tài liệu không được âm (âm sẽ làm đảo ngược logic xếp hạng).
    private void validateNonNegativeWeight(Double value) {
        if (value == null || value < 0.0) {
            throw new IllegalArgumentException("error.settings.aiWeightNonNegative");
        }
    }

    // Đọc toàn bộ giới hạn dung lượng hiện tại từ database.
    private UploadLimitsDTO readUploadLimits() {
        long maxFileLocal = systemSettingService.getLong(
                SystemSettingService.MAX_FILE_LOCAL_BYTES_KEY, SystemSettingService.MAX_FILE_LOCAL_BYTES_DEFAULT);
        long maxFileCloud = systemSettingService.getLong(
                SystemSettingService.MAX_FILE_CLOUD_BYTES_KEY, SystemSettingService.MAX_FILE_CLOUD_BYTES_DEFAULT);
        long quotaLocal = systemSettingService.getLong(
                SystemSettingService.QUOTA_LOCAL_BYTES_KEY, SystemSettingService.QUOTA_LOCAL_BYTES_DEFAULT);
        long quotaCloud = systemSettingService.getLong(
                SystemSettingService.QUOTA_CLOUD_BYTES_KEY, SystemSettingService.QUOTA_CLOUD_BYTES_DEFAULT);
        return new UploadLimitsDTO(maxFileLocal, maxFileCloud, quotaLocal, quotaCloud);
    }

    // Giới hạn dung lượng phải dương và không vượt trần cứng của Tomcat/Spring (2GB).
    private void validateUploadLimit(Long value, String label) {
        if (value == null || value <= 0 || value > MULTIPART_CEILING_BYTES) {
            throw new IllegalArgumentException(
                    "Giới hạn dung lượng " + label + " phải lớn hơn 0 và không vượt quá 2GB.");
        }
    }
}
