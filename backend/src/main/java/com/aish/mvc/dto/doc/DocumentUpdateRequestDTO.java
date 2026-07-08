package com.aish.mvc.dto.doc;

import lombok.Data;
import java.util.List;

// Request sửa metadata tài liệu. Field null = không đổi field đó.
@Data
public class DocumentUpdateRequestDTO {
    private String title;
    private String description;
    private List<Long> subjectIds; // null = giữ nguyên; nếu gửi thì phải có >=1 môn hợp lệ
}