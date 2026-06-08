package com.aish.mvc.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiChatResponse {

    private String message;
    private String mode; // "RAG" | "SYSTEM" | "GENERAL"
}