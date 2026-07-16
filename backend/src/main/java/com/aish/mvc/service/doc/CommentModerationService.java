package com.aish.mvc.service.doc;

import com.aish.mvc.dto.ai.ModerationDecision;
import com.aish.mvc.dto.ai.ModerationResultDTO;
import com.aish.mvc.entity.doc.Comment;
import com.aish.mvc.entity.enums.CommentStatus;
import com.aish.mvc.repository.doc.CommentRepository;
import com.aish.mvc.service.ai.AiModerationService;
import com.aish.mvc.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentModerationService {

    private final CommentRepository commentRepository;
    private final AiModerationService aiModerationService;
    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void screenAfterCommit(CommentModerationRequestedEvent event) {
        try {
            Comment comment = commentRepository.findWithUserAndDocumentById(event.commentId()).orElse(null);
            if (comment == null || comment.getStatus() != CommentStatus.VISIBLE) {
                return;
            }

            String screenedContent = comment.getContent();
            log.info("Bắt đầu AI kiểm duyệt bình luận {}", comment.getId());
            ModerationResultDTO result = aiModerationService.screenText(screenedContent);
            log.info("AI kiểm duyệt bình luận {} hoàn tất: {}", comment.getId(), result.getDecision());

            if (result.getDecision() != ModerationDecision.FLAG) {
                return;
            }

            int updated = commentRepository.markPendingAfterAiFlag(
                    comment.getId(), screenedContent, result.getReason());
            if (updated == 0) {
                return;
            }

            commentRepository.findWithUserAndDocumentById(comment.getId()).ifPresent(flagged -> {
                try {
                    notificationService.notifyCommentUnderReview(
                            flagged.getUser().getId(),
                            flagged.getId(),
                            flagged.getDocument().getId());
                } catch (Exception exception) {
                    log.warn("Không thể gửi thông báo AI flag cho bình luận {}; trạng thái vẫn được giữ.",
                            flagged.getId(), exception);
                }
            });
        } catch (Exception exception) {
            log.warn("AI kiểm duyệt bình luận {} bị lỗi; giữ nguyên VISIBLE để không chặn người dùng.",
                    event.commentId(), exception);
        }
    }
}
