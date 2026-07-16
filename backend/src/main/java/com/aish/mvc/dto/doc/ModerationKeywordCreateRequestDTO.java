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

    @NotBlank(message = "Từ khóa không được để trống.")
    @Size(max = 255, message = "Từ khóa không được dài quá 255 ký tự.")
    private String keyword;

    @NotNull(message = "Loại từ khóa là bắt buộc.")
    private ModerationKeywordType type;
}
