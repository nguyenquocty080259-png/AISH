package com.aish.mvc.dto.doc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private String ownerName; // Tên của người dùng đã bình luận
    private String content;   // Nội dung bình luận
    private String status;
    private LocalDateTime createdAt; // Thời gian bình luận
}
