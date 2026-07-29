package com.aish.mvc.dto.doc;

import com.aish.mvc.entity.enums.ModerationKeywordType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ModerationKeywordCreateRequestDTO {

    @NotBlank(message = "{validation.keyword.blank}")
    @Size(max = 255, message = "{validation.keyword.tooLong}")
    private String keyword;

    @NotNull(message = "{validation.keyword.typeRequired}")
    private ModerationKeywordType type;
}
