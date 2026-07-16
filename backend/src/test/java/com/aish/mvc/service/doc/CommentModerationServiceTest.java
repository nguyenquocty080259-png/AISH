package com.aish.mvc.service.doc;

import com.aish.mvc.dto.ai.ModerationDecision;
import com.aish.mvc.dto.ai.ModerationResultDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.Comment;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.CommentStatus;
import com.aish.mvc.repository.doc.CommentRepository;
import com.aish.mvc.service.ai.AiModerationService;
import com.aish.mvc.service.notification.NotificationService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommentModerationServiceTest {

    @Test
    void aiFlagMovesUnchangedVisibleCommentToPendingAndNotifies() {
        CommentRepository repository = mock(CommentRepository.class);
        AiModerationService aiService = mock(AiModerationService.class);
        NotificationService notificationService = mock(NotificationService.class);
        Comment comment = visibleComment();
        when(repository.findWithUserAndDocumentById(10L)).thenReturn(Optional.of(comment));
        when(aiService.screenText("clean-looking text"))
                .thenReturn(new ModerationResultDTO(null, ModerationDecision.FLAG, "AI reason"));
        when(repository.markPendingAfterAiFlag(10L, "clean-looking text", "AI reason")).thenReturn(1);
        CommentModerationService service = new CommentModerationService(
                repository, aiService, notificationService);

        service.screenAfterCommit(new CommentModerationRequestedEvent(10L));

        verify(repository).markPendingAfterAiFlag(10L, "clean-looking text", "AI reason");
        verify(notificationService).notifyCommentUnderReview(20L, 10L, 30L);
    }

    @Test
    void aiExceptionLeavesCommentVisibleAndDoesNotNotify() {
        CommentRepository repository = mock(CommentRepository.class);
        AiModerationService aiService = mock(AiModerationService.class);
        NotificationService notificationService = mock(NotificationService.class);
        when(repository.findWithUserAndDocumentById(10L)).thenReturn(Optional.of(visibleComment()));
        when(aiService.screenText("clean-looking text")).thenThrow(new RuntimeException("AI down"));
        CommentModerationService service = new CommentModerationService(
                repository, aiService, notificationService);

        service.screenAfterCommit(new CommentModerationRequestedEvent(10L));

        verify(repository, never()).markPendingAfterAiFlag(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
        verify(notificationService, never()).notifyCommentUnderReview(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong());
    }

    private Comment visibleComment() {
        AuthUser owner = new AuthUser();
        owner.setId(20L);
        DocDocument document = new DocDocument();
        document.setId(30L);
        return Comment.builder()
                .id(10L)
                .user(owner)
                .document(document)
                .content("clean-looking text")
                .status(CommentStatus.VISIBLE)
                .build();
    }
}
