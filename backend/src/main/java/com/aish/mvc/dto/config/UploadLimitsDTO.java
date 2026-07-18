package com.aish.mvc.dto.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Đơn vị byte ở cả API lẫn DB - quy đổi GB chỉ là chuyện của UI admin (FE).
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadLimitsDTO {
    private Long maxUploadLocalBytes;
    private Long maxUploadCloudBytes;
}
