package com.aish.mvc.dto.doc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Một dòng trong danh sách "đang chia sẻ cho ai" — CHỈ chủ sở hữu tài liệu mới xem được.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentShareRecipientDTO {
    private Long userId;
    private String fullName;
    private String email;        // email tài khoản chính của người được chia sẻ
    private String permission;   // VIEWER | COMMENTER
}
