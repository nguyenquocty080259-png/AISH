package com.aish.mvc.dto.doc;

import com.aish.mvc.entity.enums.ModerationKeywordType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ModerationKeywordResponseDTO {

    private final Long id;
    private final String keyword;
    private final ModerationKeywordType type;
    private final boolean active;
    private final LocalDateTime createdAt;
}
