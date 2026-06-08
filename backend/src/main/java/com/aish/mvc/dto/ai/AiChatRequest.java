package com.aish.mvc.dto.ai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiChatRequest {

    private String message;

    // null = không có document → mode system/general
    private Long documentId;

    // null = guest, có = user đã login
    private Long conversationId;
}