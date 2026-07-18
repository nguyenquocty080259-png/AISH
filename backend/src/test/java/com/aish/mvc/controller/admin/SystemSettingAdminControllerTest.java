package com.aish.mvc.controller.admin;

import com.aish.mvc.dto.config.UploadLimitsDTO;
import com.aish.mvc.service.config.SystemSettingService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * PUT /api/admin/settings/upload-limits: giới hạn 1 file lớn hơn quota tổng là vô lý (không
 * file nào tải lên được nữa) -> phải bị chặn ở tầng validate, không lưu xuống DB.
 * IllegalArgumentException ở đây map sang 400 qua GlobalExceptionHandler.handleBadRequest -
 * không lặp lại việc test GlobalExceptionHandler ở đây.
 */
class SystemSettingAdminControllerTest {

    private SystemSettingAdminController newController() {
        // setValue()/getLong() không được gọi tới trong các test này vì validate luôn ném
        // exception trước - mock trống là đủ.
        return new SystemSettingAdminController(mock(SystemSettingService.class));
    }

    @Test
    void rejectsWhenMaxFileLocalExceedsQuotaLocal() {
        SystemSettingAdminController controller = newController();
        UploadLimitsDTO request = new UploadLimitsDTO(2_000_000L, 1_000_000L, 1_000_000L, 1_000_000L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.updateUploadLimits(request));
        assertEquals(true, ex.getMessage().contains("LOCAL"));
    }

    @Test
    void rejectsWhenMaxFileCloudExceedsQuotaCloud() {
        SystemSettingAdminController controller = newController();
        UploadLimitsDTO request = new UploadLimitsDTO(1_000_000L, 2_000_000L, 1_000_000L, 1_000_000L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.updateUploadLimits(request));
        assertEquals(true, ex.getMessage().contains("CLOUD"));
    }

    @Test
    void acceptsWhenMaxFileEqualsQuota() {
        SystemSettingAdminController controller = newController();
        UploadLimitsDTO request = new UploadLimitsDTO(1_000_000L, 1_000_000L, 1_000_000L, 1_000_000L);

        // maxFile == quota là hợp lệ (chỉ "vượt quá" mới bị chặn) - không ném exception.
        controller.updateUploadLimits(request);
    }

    @Test
    void rejectsNonPositiveValue() {
        SystemSettingAdminController controller = newController();
        UploadLimitsDTO request = new UploadLimitsDTO(0L, 1_000_000L, 1_000_000L, 1_000_000L);

        assertThrows(IllegalArgumentException.class, () -> controller.updateUploadLimits(request));
    }

    @Test
    void rejectsValueAboveMultipartCeiling() {
        SystemSettingAdminController controller = newController();
        long aboveCeiling = 3L * 1024 * 1024 * 1024; // 3 GiB > trần 2 GiB
        UploadLimitsDTO request = new UploadLimitsDTO(aboveCeiling, 1_000_000L, aboveCeiling, 1_000_000L);

        assertThrows(IllegalArgumentException.class, () -> controller.updateUploadLimits(request));
    }
}
