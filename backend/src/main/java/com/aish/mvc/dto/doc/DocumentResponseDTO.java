package com.aish.mvc.dto.doc;

import com.aish.mvc.dto.doc.CommentDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String visibility;
    private String ownerName;
    private String fileName;
    private LocalDateTime createdAt;

    private Long favoriteCount;
    private Long downloadCount;
    private Double averageRating;

    // ĐÃ SỬA: Đổi tên từ isFavorited thành favorited
    // Lombok sẽ tự sinh chính xác hàm setFavorited() và isFavorited() mà không bị lỗi nữa!
    private Boolean favorited;

    private List<CommentDTO> comments;
    private Long subjectId;
    private String subjectName;
    private java.util.List<String> tags;
    private String fileUrl;   // tên file vật lý để xem trước
    private String fileType;  // loại file (application/pdf, image/png...)
}