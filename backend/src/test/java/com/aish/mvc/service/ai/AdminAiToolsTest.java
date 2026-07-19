package com.aish.mvc.service.ai;

import com.aish.mvc.dto.doc.AdminStatsDTO;
import com.aish.mvc.dto.ai.AiUsageStatsDTO;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.service.admin.AdminService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminAiToolsTest {

    private final AdminService adminService = mock(AdminService.class);
    private final DocDocumentRepository documents = mock(DocDocumentRepository.class);
    private final AuthAccountRepository accounts = mock(AuthAccountRepository.class);
    private final AuthUserRepository users = mock(AuthUserRepository.class);
    private final AiUsageStatsService usageStats = mock(AiUsageStatsService.class);
    private final AdminAiTools adminAiTools = new AdminAiTools(adminService, documents, accounts, users, usageStats);

    @Test
    void formatsEverySystemCounter() {
        when(adminService.getStats()).thenReturn(new AdminStatsDTO(7, 50, 38, 12, 3, 10, 42, 6, 2, 0, 0));

        String result = adminAiTools.getSystemStats();

        assertTrue(result.contains("7"));
        assertTrue(result.contains("50"));
        assertTrue(result.contains("42"));
        assertTrue(result.contains("6"));
        assertTrue(result.contains("2"));
    }

    @Test
    void returnsFriendlyMessageWhenStatsCannotBeLoaded() {
        when(adminService.getStats()).thenThrow(new RuntimeException("database unavailable"));

        assertTrue(adminAiTools.getSystemStats().startsWith("Không lấy được"));
    }

    @Test
    void formatsAiUsageAndFailsSafe() {
        AiUsageStatsDTO.CallTypeStats chat = new AiUsageStatsDTO.CallTypeStats("CHAT_GENERAL", 2, 300, 0.0002);
        when(usageStats.getStats()).thenReturn(new AiUsageStatsDTO(
                new AiUsageStatsDTO.PeriodStats(2, 300, 0.0002, List.of(chat)),
                new AiUsageStatsDTO.PeriodStats(5, 900, 0.0007, List.of(chat))));

        String result = adminAiTools.getAiUsageStats();
        assertTrue(result.contains("CHAT_GENERAL"));
        assertTrue(result.contains("300"));
        assertTrue(result.contains("900"));

        when(usageStats.getStats()).thenThrow(new RuntimeException("database unavailable"));
        assertTrue(adminAiTools.getAiUsageStats().startsWith("Không thể lấy thống kê sử dụng AI"));
    }

    @Test
    void documentSearchTreatsBlankAsNoFilterAndCapsLimit() {
        when(documents.searchForAdminTool(any(), any(), any(), any(), any())).thenReturn(List.of());

        assertEquals("Không tìm thấy tài liệu phù hợp.",
                adminAiTools.searchDocuments("  ", "  pUbLiC ", " AppROVed ", "  Toán học  ", 99));

        verify(documents).searchForAdminTool(isNull(), eq(DocumentVisibility.PUBLIC),
                eq(ModerationStatus.APPROVED), eq("Toán học"),
                argThat(pageable -> pageable.getPageSize() == 10));
    }

    @Test
    void unknownDocumentFiltersAreIgnored() {
        when(documents.searchForAdminTool(any(), any(), any(), any(), any())).thenReturn(List.of());

        adminAiTools.searchDocuments("tích phân", "công cộng lạ", "không rõ", null, null);

        verify(documents).searchForAdminTool(eq("tích phân"), isNull(), isNull(), isNull(),
                argThat(pageable -> pageable.getPageSize() == 5));
    }

    @Test
    void userSearchNormalizesOddCaseAndIgnoresUnknownRole() {
        when(accounts.searchUsersForAdminTool(any(), any(), any(), any())).thenReturn(List.of());

        adminAiTools.searchUsers("  Nguyễn  ", "quản trị lạ", " aCtIvE ", 0);

        verify(accounts).searchUsersForAdminTool(eq("Nguyễn"), isNull(), eq(UserStatus.ACTIVE),
                argThat(pageable -> pageable.getPageSize() == 1));
    }

    @Test
    void lookupNotFoundIsFriendly() {
        when(documents.findStatusByIdForAdminTool(34L)).thenReturn(Optional.empty());

        assertEquals("Không tìm thấy tài liệu ID 34.", adminAiTools.getDocumentStatus(34L));
        assertEquals("Vui lòng cung cấp ID hoặc email người dùng.", adminAiTools.getUserStatus("  "));
    }

    @Test
    void toolFailureNeverEscapesIntoChatTurn() {
        when(documents.searchForAdminTool(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("database unavailable"));

        assertTrue(adminAiTools.searchDocuments(null, null, null, null, null)
                .startsWith("Không thể tìm tài liệu"));
    }
}
