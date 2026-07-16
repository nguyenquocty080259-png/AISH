package com.aish.mvc.dto.doc;

import com.aish.mvc.entity.enums.CommentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminCommentReviewDTO {
    private final Long id;
    private final String content;
    private final String authorName;
    private final Long documentId;
    private final String documentTitle;
    private final String moderationReason;
    private final String disputeNote;
    private final LocalDateTime createdAt;
    private final CommentStatus status;
}
