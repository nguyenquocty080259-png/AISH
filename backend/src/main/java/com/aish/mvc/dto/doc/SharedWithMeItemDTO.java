package com.aish.mvc.dto.doc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SharedWithMeItemDTO {
    private DocumentResponseDTO document;
    private String permission;   // VIEWER | COMMENTER
    private String sharedByName; // tên người đã chia sẻ
}
