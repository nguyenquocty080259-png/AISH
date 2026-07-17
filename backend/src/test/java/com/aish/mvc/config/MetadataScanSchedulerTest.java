package com.aish.mvc.config;

import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.IngestStatus;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.entity.enums.NotificationType;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.service.ai.AiModerationService;
import com.aish.mvc.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MetadataScanSchedulerTest {
    private DocDocumentRepository documents;
    private AiModerationService moderation;
    private NotificationService notifications;
    private AuthUserRepository users;
    private MetadataScanScheduler scheduler;

    @BeforeEach
    void setUp() {
        documents = mock(DocDocumentRepository.class);
        moderation = mock(AiModerationService.class);
        notifications = mock(NotificationService.class);
        users = mock(AuthUserRepository.class);
        scheduler = spy(new MetadataScanScheduler(documents, moderation, notifications, users));
        doNothing().when(scheduler).sleepBetweenCalls();
    }

    @Test
    void requestsOnlyEligibleCandidatesWithFiftyDocumentCap() {
        when(documents.findMetadataScanCandidates(any(), any(), any(), any())).thenReturn(List.of());

        scheduler.scanMetadata();

        verify(documents).findMetadataScanCandidates(eq(DocumentVisibility.PUBLIC),
                eq(ModerationStatus.APPROVED), eq(IngestStatus.INGESTED),
                argThat(page -> page.getPageNumber() == 0 && page.getPageSize() == 50));
    }

    @Test
    void failedDocumentIsNotStampedAndDoesNotStopNextCandidate() {
        DocDocument failed = document(1L, null);
        DocDocument matching = document(2L, null);
        when(documents.findMetadataScanCandidates(any(), any(), any(), any()))
                .thenReturn(List.of(failed, matching));
        when(moderation.checkMetadata(failed)).thenThrow(new RuntimeException("Groq unavailable"));
        when(moderation.checkMetadata(matching))
                .thenReturn(new AiModerationService.MetadataMatchResult("KHOP", "-"));

        scheduler.scanMetadata();

        verify(documents, never()).stampMetadataCheck(eq(1L), anyString(), any());
        verify(documents).stampMetadataCheck(eq(2L), eq("KHOP"), any());
        verifyNoInteractions(notifications);
    }

    @Test
    void mismatchTransitionNotifiesOwnerAndAdminsOnlyOnce() {
        DocDocument document = document(3L, null);
        AuthUser admin = mock(AuthUser.class);
        when(admin.getId()).thenReturn(99L);
        when(users.findByRole_RoleNameAndStatus("ADMIN", UserStatus.ACTIVE)).thenReturn(List.of(admin));
        when(moderation.checkMetadata(document))
                .thenReturn(new AiModerationService.MetadataMatchResult("LECH", "Sai môn học"));

        scheduler.processDocument(document);
        scheduler.processDocument(document);

        verify(notifications, times(1)).createDocumentNotification(eq(10L),
                eq(NotificationType.METADATA_MISMATCH), contains("AI gợi ý"), eq(3L));
        verify(notifications, times(1)).createDocumentNotification(eq(99L),
                eq(NotificationType.METADATA_MISMATCH), contains("Sai môn học"), eq(3L));
    }

    @Test
    void transitionRuleOnlyTriggersWhenEnteringMismatch() {
        assertTrue(MetadataScanScheduler.shouldNotify(null, "LECH"));
        assertTrue(MetadataScanScheduler.shouldNotify("KHOP", "LECH"));
        assertFalse(MetadataScanScheduler.shouldNotify("LECH", "LECH"));
        assertFalse(MetadataScanScheduler.shouldNotify("LECH", "KHOP"));
        assertFalse(MetadataScanScheduler.shouldNotify(null, "KHOP"));
    }

    private static DocDocument document(Long id, String status) {
        AuthUser owner = mock(AuthUser.class);
        when(owner.getId()).thenReturn(10L);
        return DocDocument.builder().id(id).title("Document " + id).user(owner)
                .metadataMatchStatus(status).build();
    }
}
