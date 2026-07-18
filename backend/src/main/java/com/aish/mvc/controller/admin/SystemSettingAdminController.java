package com.aish.mvc.controller.admin;

import com.aish.mvc.dto.config.MinUploadAgeDTO;
import com.aish.mvc.dto.config.UploadLimitsDTO;
import com.aish.mvc.service.config.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class SystemSettingAdminController {

    // Trần cứng của spring.servlet.multipart.max-file-size (application.properties) - giới
    // hạn admin cấu hình không được vượt quá trần này, nếu không request sẽ bị Tomcat/Spring
    // chặn ở tầng multipart trước khi tới được gate của DocumentServiceImpl.
    private static final long MULTIPART_CEILING_BYTES = 2L * 1024 * 1024 * 1024; // 2 GiB

    private final SystemSettingService systemSettingService;

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
            throw new IllegalArgumentException("Tuổi tối thiểu để tải tài liệu lên phải từ 6 đến 100.");
        }
        systemSettingService.setValue(SystemSettingService.MIN_UPLOAD_AGE_KEY, String.valueOf(value));
        return ResponseEntity.ok(new MinUploadAgeDTO(value));
    }

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

    private void validateUploadLimit(Long value, String label) {
        if (value == null || value <= 0 || value > MULTIPART_CEILING_BYTES) {
            throw new IllegalArgumentException(
                    "Giới hạn dung lượng " + label + " phải lớn hơn 0 và không vượt quá 2GB.");
        }
    }
}
