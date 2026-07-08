package com.aish.mvc.dto.ai;

import lombok.Getter;

import java.util.List;

@Getter
public class AiChatResponse {

    private String answer;
    private String mode; // "RAG" | "GENERAL"
    private List<CitationDTO> citations;
    private List<RelatedDocDTO> relatedDocs;
    private Long conversationId;

    public AiChatResponse(String answer, String mode, List<CitationDTO> citations, List<RelatedDocDTO> relatedDocs) {
        this(answer, mode, citations, relatedDocs, null);
    }

    public AiChatResponse(String answer, String mode, List<CitationDTO> citations, List<RelatedDocDTO> relatedDocs, Long conversationId) {
        this.answer = answer;
        this.mode = mode;
        this.citations = citations;
        this.relatedDocs = relatedDocs;
        this.conversationId = conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
}
