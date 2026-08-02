package com.aish.mvc.service.doc.impl;

import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.Comment;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.Download;
import com.aish.mvc.entity.doc.Favorite;
import com.aish.mvc.entity.doc.Rating;
import com.aish.mvc.entity.doc.ViewHistory;
import com.aish.mvc.entity.enums.CommentStatus;
import com.aish.mvc.entity.enums.ModerationKeywordType;
import com.aish.mvc.entity.enums.NotificationType;
import com.aish.mvc.exception.CommentBlockedException;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.CommentRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.DownloadRepository;
import com.aish.mvc.repository.doc.FavoriteRepository;
import com.aish.mvc.repository.doc.RatingRepository;
import com.aish.mvc.repository.doc.ViewHistoryRepository;
import com.aish.mvc.service.doc.DocumentAccessPort;
import com.aish.mvc.service.doc.EngagementService;
import com.aish.mvc.service.doc.CommentModerationRequestedEvent;
import com.aish.mvc.service.ai.ToxicKeywordFilter;
import com.aish.mvc.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aish.mvc.exception.ForbiddenException;

import java.time.LocalDateTime;

/**
 * TƯƠNG TÁC của người dùng với tài liệu: bình luận (kèm lọc từ khoá + kiểm duyệt AI), yêu thích,
 * chấm sao, và ghi nhận lượt xem / lượt tải.
 *
 * <p>Điểm chung: bình luận, yêu thích, chấm sao đều là thao tác TRÊN nội dung nên trước khi làm
 * gì cũng phải kiểm tra người dùng thật sự được xem tài liệu đó (dùng chung
 * {@link DocumentAccessPort} với các module khác, không tự chép lại luật quyền).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EngagementServiceImpl implements EngagementService {

    private static final String KEYWORD_REASON =
            "Bình luận chứa từ khóa không phù hợp theo quy tắc kiểm duyệt.";

    // Thang sao hiển thị ở FE (RatingStars) là 1-5; BE phải tự chốt lại vì client nào cũng
    // gọi thẳng API được, và điểm rác chảy vào averageRating của cả tài liệu lẫn bộ lọc
    // "điểm tối thiểu" ở trang Cộng đồng.
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final DocDocumentRepository docDocumentRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;
    private final RatingRepository ratingRepository;
    private final DownloadRepository downloadRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final AuthAccountRepository authAccountRepository;
    private final ToxicKeywordFilter toxicKeywordFilter;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final DocumentAccessPort documentAccessPort;

    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    private DocDocument requireDocument(Long documentId) {
        return docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));
    }

    // Bình luận / đánh giá / yêu thích đều là thao tác TRÊN nội dung, nên chỉ hợp lệ khi người
    // dùng thật sự được xem tài liệu đó. Tái dùng DocumentAccessPort — cùng luật availability
    // (chủ sở hữu / PUBLIC / được chia sẻ, và tài liệu chưa bị xoá mềm) mà Collections và AI
    // chat đang dùng — thay vì chép lại điều kiện quyền ở đây.
    private void requireReadableDocument(Long documentId) {
        if (!documentAccessPort.isAvailableTo(documentId, getCurrentUser().getId())) {
            throw new ForbiddenException("error.engagement.docForbidden");
        }
    }

    /**
     * THÊM BÌNH LUẬN vào tài liệu.
     *
     * <p>Đầu vào: id tài liệu, nội dung, cờ khiếu nại và lý do khiếu nại. Không trả về gì.
     *
     * <p>Các bước: (1) tài liệu phải tồn tại và người dùng phải được xem nó, (2) làm sạch nội
     * dung, (3) quét từ khoá cấm, (4) trúng từ khoá mà không khiếu nại -> chặn luôn; có khiếu
     * nại -> lưu ở trạng thái chờ Admin duyệt, (5) lưu bình luận, (6) bình luận sạch thì gửi
     * đi kiểm duyệt AI ở chế độ nền và báo cho chủ tài liệu.
     */
    @Override
    @Transactional
    public void addComment(Long documentId, String content, boolean dispute, String disputeNote) {
        DocDocument doc = requireDocument(documentId);
        requireReadableDocument(documentId);
        String cleaned = validateContent(content);
        // B3: đối chiếu nội dung với danh sách từ khoá cấm loại COMMENT do Admin quản lý.
        boolean keywordHit = toxicKeywordFilter.matches(cleaned, ModerationKeywordType.COMMENT);
        // B4: trúng từ khoá và người dùng KHÔNG khiếu nại -> chặn, không lưu gì cả.
        if (keywordHit && !dispute) {
            throw new CommentBlockedException(KEYWORD_REASON);
        }
        Comment comment = Comment.builder()
                .document(doc)
                .user(getCurrentUser())
                .content(cleaned)
                // PENDING_REVIEW = ẩn với người khác, chờ Admin duyệt. VISIBLE = hiện bình thường.
                .status(keywordHit ? CommentStatus.PENDING_REVIEW : CommentStatus.VISIBLE)
                .moderationReason(keywordHit ? KEYWORD_REASON : null)
                .disputeNote(keywordHit ? validateDisputeNote(disputeNote) : null)
                .build();
        // B5: ghi bình luận xuống database (bảng comments: tài liệu, người viết, nội dung, trạng thái).
        Comment saved = commentRepository.save(comment);
        if (keywordHit) {
            notifyUnderReview(saved); // báo người viết là bình luận đang chờ duyệt
        } else {
            // B6: bắn sự kiện để AI kiểm duyệt chạy NGẦM sau khi lưu xong — người dùng không phải
            // chờ AI, bình luận hiện ngay; nếu AI thấy có vấn đề thì mới chuyển sang chờ duyệt.
            eventPublisher.publishEvent(new CommentModerationRequestedEvent(saved.getId()));
        }
        if (saved.getStatus() == CommentStatus.VISIBLE
                && !saved.getUser().getId().equals(doc.getUser().getId())) {
            notifyCommentOnMyDoc(doc);
        }
    }

    /**
     * BẤM TIM / BỎ TIM (một endpoint làm cả hai việc). Đầu vào: id tài liệu.
     * Đã thích rồi thì xoá dòng trong bảng favorites, chưa thích thì thêm dòng mới.
     */
    @Override
    @Transactional
    public void toggleFavorite(Long documentId) {
        requireDocument(documentId);
        requireReadableDocument(documentId);
        Long uid = getCurrentUser().getId();
        if (favoriteRepository.existsByUserIdAndDocumentId(uid, documentId)) {
            favoriteRepository.deleteByUserIdAndDocumentId(uid, documentId); // đang thích -> bỏ thích
        } else {
            Favorite favorite = Favorite.builder()
                    .userId(uid)
                    .documentId(documentId)
                    .createdAt(LocalDateTime.now())
                    .build();
            // Ghi xuống database (bảng favorites: ai thích tài liệu nào, lúc nào).
            favoriteRepository.save(favorite);
        }
    }

    /**
     * CHẤM SAO cho tài liệu. Đầu vào: id tài liệu + số sao (1-5).
     *
     * <p>Mỗi người chỉ có MỘT điểm cho mỗi tài liệu: chấm lại là ghi đè điểm cũ, không tạo dòng
     * mới — nhờ vậy không ai tự "cày" điểm cho tài liệu của mình bằng cách bấm nhiều lần.
     */
    @Override
    @Transactional
    public void rateDocument(Long documentId, Integer star) {
        // Chốt lại khoảng 1-5 ở backend: giao diện có giới hạn rồi nhưng ai cũng gọi thẳng API được.
        if (star == null || star < MIN_RATING || star > MAX_RATING) {
            throw new IllegalArgumentException(
                    "Điểm đánh giá phải là số nguyên từ " + MIN_RATING + " đến " + MAX_RATING + ".");
        }
        DocDocument doc = requireDocument(documentId);
        requireReadableDocument(documentId);
        Long uid = getCurrentUser().getId();
        Rating rating = ratingRepository.findByUserIdAndDocument_Id(uid, documentId)
                .orElseGet(() -> Rating.builder()
                        .userId(uid)
                        .document(doc)
                        .createdAt(LocalDateTime.now())
                        .build());
        boolean isNewRating = rating.getId() == null; // chưa có id nghĩa là lần chấm đầu tiên
        rating.setRating(star);
        // Ghi xuống database (bảng ratings): có sẵn thì cập nhật, chưa có thì thêm dòng mới.
        ratingRepository.save(rating);
        // Chỉ báo cho chủ tài liệu ở LẦN CHẤM ĐẦU, và không tự báo cho chính mình.
        if (isNewRating && !uid.equals(doc.getUser().getId())) {
            notifyRatingOnMyDoc(doc);
        }
    }

    /**
     * GHI NHẬN LƯỢT XEM vào lịch sử (phục vụ mục "Tiếp tục học"). Đầu vào: id tài liệu.
     * Mỗi cặp (người xem, tài liệu) chỉ có MỘT dòng — xem lại thì chỉ cập nhật thời điểm xem.
     */
    @Override
    @Transactional
    public void logView(Long documentId) {
        AuthUser user = getCurrentUser();
        ViewHistory vh = viewHistoryRepository.findByUser_IdAndDocumentId(user.getId(), documentId)
                .orElseGet(() -> ViewHistory.builder()
                        .user(user)
                        .documentId(documentId)
                        .build());
        vh.setViewedAt(LocalDateTime.now());
        // Ghi xuống database (bảng view_history: ai xem tài liệu nào, lần cuối lúc nào).
        viewHistoryRepository.save(vh);
    }

    /**
     * GHI NHẬN LƯỢT TẢI. Đầu vào: id tài liệu. Khác lượt xem: mỗi lần tải là MỘT dòng mới,
     * nên đếm được tổng số lượt tải để xếp hạng "tải nhiều nhất" ở trang Cộng đồng.
     */
    @Override
    @Transactional
    public void logDownload(Long documentId) {
        DocDocument doc = requireDocument(documentId);
        Download download = Download.builder()
                .userId(getCurrentUser().getId())
                .document(doc)
                .downloadedAt(LocalDateTime.now())
                .build();
        // Ghi xuống database (bảng downloads: ai tải tài liệu nào, lúc nào).
        downloadRepository.save(download);
    }
    /**
     * SỬA BÌNH LUẬN — chỉ tác giả. Đầu vào: id bình luận + nội dung mới (+ khiếu nại nếu có).
     *
     * <p>Nội dung mới bị lọc từ khoá lại từ đầu, và kết quả duyệt trước đó bị xoá đi — tránh
     * việc viết nội dung sạch để được duyệt rồi sửa thành nội dung xấu.
     */
    @Override
    @Transactional
    public void updateComment(Long commentId, String content, boolean dispute, String disputeNote) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận không tồn tại!"));
        if (!comment.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("error.comment.editForbidden");
        }
        String cleaned = validateContent(content);
        boolean keywordHit = toxicKeywordFilter.matches(cleaned, ModerationKeywordType.COMMENT);
        if (keywordHit && !dispute) {
            throw new CommentBlockedException(KEYWORD_REASON);
        }

        comment.setContent(cleaned);
        comment.setStatus(keywordHit ? CommentStatus.PENDING_REVIEW : CommentStatus.VISIBLE);
        comment.setModerationReason(keywordHit ? KEYWORD_REASON : null);
        comment.setDisputeNote(keywordHit ? validateDisputeNote(disputeNote) : null);
        comment.setReviewedBy(null);
        comment.setReviewedAt(null);
        Comment saved = commentRepository.save(comment);
        if (keywordHit) {
            notifyUnderReview(saved);
        } else {
            eventPublisher.publishEvent(new CommentModerationRequestedEvent(saved.getId()));
        }
    }

    /** XOÁ BÌNH LUẬN — chỉ tác giả. Đầu vào: id bình luận. Xoá hẳn dòng khỏi bảng comments. */
    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận không tồn tại!"));
        if (!comment.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("error.comment.deleteForbidden");
        }
        commentRepository.delete(comment);
    }

    private String validateContent(String content) {
        String cleaned = content == null ? "" : content.trim();
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("error.comment.empty");
        }
        return cleaned;
    }

    private String validateDisputeNote(String disputeNote) {
        String cleaned = disputeNote == null ? "" : disputeNote.trim();
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("error.complaint.reasonRequired");
        }
        return cleaned;
    }

    private void notifyUnderReview(Comment comment) {
        try {
            notificationService.notifyCommentUnderReview(
                    comment.getUser().getId(), comment.getId(), comment.getDocument().getId());
        } catch (Exception exception) {
            log.warn("Không thể gửi thông báo cho bình luận {} đang chờ duyệt; luồng bình luận vẫn tiếp tục.",
                    comment.getId(), exception);
        }
    }

    private void notifyCommentOnMyDoc(DocDocument doc) {
        try {
            notificationService.createDocumentNotification(
                    doc.getUser().getId(),
                    NotificationType.COMMENT_ON_MY_DOC,
                    "Có bình luận mới trên tài liệu \"" + doc.getTitle() + "\" của bạn.",
                    doc.getId());
        } catch (Exception exception) {
            log.warn("Không thể gửi thông báo bình luận mới cho chủ tài liệu {}; bình luận vẫn được lưu.",
                    doc.getId(), exception);
        }
    }

    private void notifyRatingOnMyDoc(DocDocument doc) {
        try {
            notificationService.createDocumentNotification(
                    doc.getUser().getId(),
                    NotificationType.RATING_ON_MY_DOC,
                    "Có đánh giá mới trên tài liệu \"" + doc.getTitle() + "\" của bạn.",
                    doc.getId());
        } catch (Exception exception) {
            log.warn("Không thể gửi thông báo đánh giá mới cho chủ tài liệu {}; đánh giá vẫn được lưu.",
                    doc.getId(), exception);
        }
    }
}
