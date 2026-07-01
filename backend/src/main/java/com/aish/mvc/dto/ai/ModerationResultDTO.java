package com.aish.mvc.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ModerationResultDTO {

    private Long documentId;
    private ModerationDecision decision; // "PASS" | "FLAG"
    private String reason;
}
