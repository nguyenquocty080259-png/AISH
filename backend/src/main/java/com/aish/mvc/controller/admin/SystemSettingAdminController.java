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
        long local = systemSettingService.getLong(
                SystemSettingService.MAX_UPLOAD_LOCAL_BYTES_KEY, SystemSettingService.MAX_UPLOAD_LOCAL_BYTES_DEFAULT);
        long cloud = systemSettingService.getLong(
                SystemSettingService.MAX_UPLOAD_CLOUD_BYTES_KEY, SystemSettingService.MAX_UPLOAD_CLOUD_BYTES_DEFAULT);
        return ResponseEntity.ok(new UploadLimitsDTO(local, cloud));
    }

    @PutMapping("/upload-limits")
    public ResponseEntity<UploadLimitsDTO> updateUploadLimits(@RequestBody UploadLimitsDTO request) {
        Long local = request.getMaxUploadLocalBytes();
        Long cloud = request.getMaxUploadCloudBytes();
        validateUploadLimit(local, "LOCAL");
        validateUploadLimit(cloud, "CLOUD");

        systemSettingService.setValue(SystemSettingService.MAX_UPLOAD_LOCAL_BYTES_KEY, String.valueOf(local));
        systemSettingService.setValue(SystemSettingService.MAX_UPLOAD_CLOUD_BYTES_KEY, String.valueOf(cloud));
        return ResponseEntity.ok(new UploadLimitsDTO(local, cloud));
    }

    private void validateUploadLimit(Long value, String storageLabel) {
        if (value == null || value <= 0 || value > MULTIPART_CEILING_BYTES) {
            throw new IllegalArgumentException(
                    "Giới hạn dung lượng cho nơi lưu " + storageLabel
                            + " phải lớn hơn 0 và không vượt quá 2GB.");
        }
    }
}
