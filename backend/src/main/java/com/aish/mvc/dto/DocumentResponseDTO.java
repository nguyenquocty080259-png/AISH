package com.aish.mvc.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentResponseDTO {
    // Chỉ lấy những trường cần thiết cho Front-end hiển thị lên màn hình
    private Long id;
    private String title;
    private String description;
    private String fileName;
    private String storageUrl;
    private String visibility;
    private LocalDateTime createdAt;
    
    // Tuyệt đối không nhét userId hay mấy thứ bảo mật vào đây
}