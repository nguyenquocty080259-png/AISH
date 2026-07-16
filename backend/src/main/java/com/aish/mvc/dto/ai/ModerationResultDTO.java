package com.aish.mvc.dto.ai;

import lombok.Getter;

@Getter
public class ModerationResultDTO {

    private Long documentId;
    private ModerationDecision decision; // "PASS" | "FLAG"
    private String reason;

    private boolean metadataMismatch;
    private String metadataMismatchReason;

    public ModerationResultDTO(Long documentId, ModerationDecision decision, String reason) {
        this(documentId, decision, reason, false, "-");
    }

    public ModerationResultDTO(Long documentId, ModerationDecision decision, String reason,
                               boolean metadataMismatch, String metadataMismatchReason) {
        this.documentId = documentId;
        this.decision = decision;
        this.reason = reason;
        this.metadataMismatch = metadataMismatch;
        this.metadataMismatchReason = metadataMismatchReason;
    }
}
