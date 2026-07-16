package com.aish.mvc.service.doc;

public interface EngagementService {
    void addComment(Long documentId, String content, boolean dispute, String disputeNote);
    void toggleFavorite(Long documentId);
    void rateDocument(Long documentId, Integer star);
    void logDownload(Long documentId);
    void logView(Long documentId);
    void updateComment(Long commentId, String content, boolean dispute, String disputeNote);
    void deleteComment(Long commentId);
}
