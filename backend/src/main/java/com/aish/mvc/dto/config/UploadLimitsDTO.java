package com.aish.mvc.dto.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Đơn vị byte ở cả API lẫn DB - quy đổi GB chỉ là chuyện của UI admin (FE).
// maxFile* = giới hạn 1 file; quota* = tổng dung lượng cho phép của 1 user (chung cho mọi user).
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadLimitsDTO {
    private Long maxFileLocalBytes;
    private Long maxFileCloudBytes;
    private Long quotaLocalBytes;
    private Long quotaCloudBytes;
}
