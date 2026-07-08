package com.aish.mvc.service.doc;

public interface EngagementService {
    void addComment(Long documentId, String content);
    void toggleFavorite(Long documentId);
    void rateDocument(Long documentId, Integer star);
    void logDownload(Long documentId);
    void logView(Long documentId);
    void updateComment(Long commentId, String content);
    void deleteComment(Long commentId);
}