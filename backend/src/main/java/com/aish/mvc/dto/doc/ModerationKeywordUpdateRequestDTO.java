package com.aish.mvc.dto.doc;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ModerationKeywordUpdateRequestDTO {

    @Size(max = 255, message = "Từ khóa không được dài quá 255 ký tự.")
    private String keyword;

    private Boolean active;
}
