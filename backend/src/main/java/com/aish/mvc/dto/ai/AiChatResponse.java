package com.aish.mvc.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AiChatResponse {

    private String answer;
    private String mode; // "RAG" | "GENERAL"
    private List<CitationDTO> citations;
    private List<Object> relatedDocs; // luôn [] ở MVP này
}
