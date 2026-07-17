package com.aish.mvc.controller.admin;

import com.aish.mvc.dto.config.MinUploadAgeDTO;
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
}
