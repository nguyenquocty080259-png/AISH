package com.aish.mvc.dto.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// Danh sách đuôi tệp được phép tải lên (viết thường, không kèm dấu chấm), áp dụng chung mọi user.
// FE hiển thị/sửa, BE chuẩn hoá rồi lưu về system_settings dạng chuỗi ngăn cách bằng dấu phẩy.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileTypesDTO {
    private List<String> allowedExtensions;
}
