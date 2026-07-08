package com.aish.mvc.service.doc.impl;

import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.Comment;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.Download;
import com.aish.mvc.entity.doc.Favorite;
import com.aish.mvc.entity.doc.Rating;
import com.aish.mvc.entity.doc.ViewHistory;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.CommentRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.DownloadRepository;
import com.aish.mvc.repository.doc.FavoriteRepository;
import com.aish.mvc.repository.doc.RatingRepository;
import com.aish.mvc.repository.doc.ViewHistoryRepository;
import com.aish.mvc.service.doc.EngagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aish.mvc.exception.ForbiddenException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EngagementServiceImpl implements EngagementService {

    private final DocDocumentRepository docDocumentRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;
    private final RatingRepository ratingRepository;
    private final DownloadRepository downloadRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final AuthAccountRepository authAccountRepository;

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
    public void addComment(Long documentId, String content) {
        DocDocument doc = requireDocument(documentId);
        Comment comment = Comment.builder()
                .document(doc)
                .user(getCurrentUser())
                .content(content)
                .build();
        commentRepository.save(comment);
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
    public void updateComment(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận không tồn tại!"));
        if (!comment.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("Bạn không có quyền sửa bình luận này!");
        }
        comment.setContent(content);
        commentRepository.save(comment);
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
}