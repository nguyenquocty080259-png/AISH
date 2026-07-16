package com.aish.mvc.service.ai;

import com.aish.mvc.dto.doc.AdminStatsDTO;
import com.aish.mvc.service.admin.AdminService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminAiToolsTest {

    private final AdminService adminService = mock(AdminService.class);
    private final AdminAiTools adminAiTools = new AdminAiTools(adminService);

    @Test
    void formatsEverySystemCounter() {
        when(adminService.getStats()).thenReturn(new AdminStatsDTO(7, 50, 38, 12, 3, 10, 42, 6, 2));

        assertEquals("""
                Tổng người dùng: 7
                Tổng tài liệu: 50
                Tài liệu công khai: 38
                Tài liệu riêng tư: 12
                Kháng nghị đang chờ: 3
                Tổng chủ đề: 10
                Tài liệu đã ingest: 42
                Tài liệu chưa ingest: 6
                Tài liệu không hỗ trợ ingest: 2""", adminAiTools.getSystemStats());
    }

    @Test
    void returnsFriendlyMessageWhenStatsCannotBeLoaded() {
        when(adminService.getStats()).thenThrow(new RuntimeException("database unavailable"));

        assertEquals("Không lấy được số liệu hệ thống lúc này.", adminAiTools.getSystemStats());
    }
}
