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

/**
 * KIỂM DUYỆT BÌNH LUẬN BẰNG AI, chạy NGẦM sau khi bình luận đã được lưu.
 *
 * <p>Vì sao chạy ngầm: gọi AI mất vài giây, nếu chờ thì người dùng bấm gửi xong phải đứng nhìn.
 * Cách làm: EngagementServiceImpl lưu bình luận rồi bắn ra một "sự kiện"; lớp này lắng nghe sự
 * kiện đó và chỉ chạy SAU KHI giao dịch lưu đã hoàn tất (AFTER_COMMIT), trên luồng riêng (@Async).
 *
 * <p>AI thấy có vấn đề (FLAG) thì chuyển bình luận sang trạng thái chờ Admin duyệt và báo cho
 * người viết. AI lỗi thì giữ nguyên bình luận hiện — thà lọt một bình luận còn hơn chặn oan cả
 * hệ thống khi dịch vụ AI trục trặc.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentModerationService {

    private final CommentRepository commentRepository;
    private final AiModerationService aiModerationService;
    private final NotificationService notificationService;

    /**
     * Chạy khi có bình luận mới/vừa sửa cần AI xem qua.
     *
     * <p>Đầu vào: sự kiện mang id bình luận. Không trả về gì.
     *
     * <p>Các bước: (1) nạp lại bình luận, chỉ xử lý bình luận đang hiện; (2) gọi AI chấm nội
     * dung; (3) AI không gắn cờ thì thôi; (4) AI gắn cờ (FLAG) thì chuyển sang chờ Admin duyệt;
     * (5) báo cho người viết biết bình luận đang chờ duyệt.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void screenAfterCommit(CommentModerationRequestedEvent event) {
        try {
            Comment comment = commentRepository.findWithUserAndDocumentById(event.commentId()).orElse(null);
            // Bình luận đã bị xoá, hoặc đã bị chuyển sang chờ duyệt bởi bộ lọc từ khoá -> khỏi làm gì.
            if (comment == null || comment.getStatus() != CommentStatus.VISIBLE) {
                return;
            }

            String screenedContent = comment.getContent();
            log.info("Bắt đầu AI kiểm duyệt bình luận {}", comment.getId());
            ModerationResultDTO result = aiModerationService.screenText(screenedContent);
            log.info("AI kiểm duyệt bình luận {} hoàn tất: {}", comment.getId(), result.getDecision());

            // B3: AI thấy bình thường -> giữ nguyên, kết thúc.
            if (result.getDecision() != ModerationDecision.FLAG) {
                return;
            }

            // B4: chuyển bình luận sang PENDING_REVIEW (ẩn với người khác, chờ Admin xử lý).
            // Câu lệnh update có kèm điều kiện nội dung phải còn y như lúc AI đọc — nếu trong lúc
            // AI chạy mà người dùng đã sửa bình luận thì không áp kết quả cũ nữa (updated == 0).
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
