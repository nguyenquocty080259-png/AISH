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
import com.aish.mvc.exception.CommentBlockedException;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.CommentRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.DownloadRepository;
import com.aish.mvc.repository.doc.FavoriteRepository;
import com.aish.mvc.repository.doc.RatingRepository;
import com.aish.mvc.repository.doc.ViewHistoryRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class EngagementServiceImpl implements EngagementService {

    private static final String KEYWORD_REASON =
            "Bình luận chứa từ khóa không phù hợp theo quy tắc kiểm duyệt.";

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

    @Override
    @Transactional
    public void addComment(Long documentId, String content, boolean dispute, String disputeNote) {
        DocDocument doc = requireDocument(documentId);
        String cleaned = validateContent(content);
        boolean keywordHit = toxicKeywordFilter.matches(cleaned, ModerationKeywordType.COMMENT);
        if (keywordHit && !dispute) {
            throw new CommentBlockedException(KEYWORD_REASON);
        }
        Comment comment = Comment.builder()
                .document(doc)
                .user(getCurrentUser())
                .content(cleaned)
                .status(keywordHit ? CommentStatus.PENDING_REVIEW : CommentStatus.VISIBLE)
                .moderationReason(keywordHit ? KEYWORD_REASON : null)
                .disputeNote(keywordHit ? validateDisputeNote(disputeNote) : null)
                .build();
        Comment saved = commentRepository.save(comment);
        if (keywordHit) {
            notifyUnderReview(saved);
        } else {
            eventPublisher.publishEvent(new CommentModerationRequestedEvent(saved.getId()));
        }
    }

    @Override
    @Transactional
    public void toggleFavorite(Long documentId) {
        Long uid = getCurrentUser().getId();
        if (favoriteRepository.existsByUserIdAndDocumentId(uid, documentId)) {
            favoriteRepository.deleteByUserIdAndDocumentId(uid, documentId);
        } else {
            Favorite favorite = Favorite.builder()
                    .userId(uid)
                    .documentId(documentId)
                    .createdAt(LocalDateTime.now())
                    .build();
            favoriteRepository.save(favorite);
        }
    }

    @Override
    @Transactional
    public void rateDocument(Long documentId, Integer star) {
        DocDocument doc = requireDocument(documentId);
        Long uid = getCurrentUser().getId();
        Rating rating = ratingRepository.findByUserIdAndDocument_Id(uid, documentId)
                .orElseGet(() -> Rating.builder()
                        .userId(uid)
                        .document(doc)
                        .createdAt(LocalDateTime.now())
                        .build());
        rating.setRating(star);
        ratingRepository.save(rating);
    }

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
        viewHistoryRepository.save(vh);
    }

    @Override
    @Transactional
    public void logDownload(Long documentId) {
        DocDocument doc = requireDocument(documentId);
        Download download = Download.builder()
                .userId(getCurrentUser().getId())
                .document(doc)
                .downloadedAt(LocalDateTime.now())
                .build();
        downloadRepository.save(download);
    }
    @Override
    @Transactional
    public void updateComment(Long commentId, String content, boolean dispute, String disputeNote) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận không tồn tại!"));
        if (!comment.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("Bạn không có quyền sửa bình luận này!");
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

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận không tồn tại!"));
        if (!comment.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("Bạn không có quyền xoá bình luận này!");
        }
        commentRepository.delete(comment);
    }

    private String validateContent(String content) {
        String cleaned = content == null ? "" : content.trim();
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("Nội dung bình luận không được để trống.");
        }
        return cleaned;
    }

    private String validateDisputeNote(String disputeNote) {
        String cleaned = disputeNote == null ? "" : disputeNote.trim();
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do khiếu nại.");
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
}
