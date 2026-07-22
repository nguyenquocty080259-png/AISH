package com.aish.mvc.dto.doc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShareResponseDTO {
    private String shareMode;
    // Chỉ có giá trị khi mode = ANYONE_WITH_LINK; FE dựng link từ token này.
    private String shareToken;
}
