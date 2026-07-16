package com.aish.mvc.dto.doc;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentBlockedResponseDTO {
    private final boolean blocked;
    private final String reason;
}
