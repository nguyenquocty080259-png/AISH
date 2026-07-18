package com.aish.mvc.dto.doc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Đơn vị byte - quy đổi GB chỉ là chuyện của UI (xem StorageUsageBar.jsx). Luôn tính cho
// người dùng đang đăng nhập (token), không bao giờ trả số liệu của user khác.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StorageUsageDTO {
    private Long usedLocalBytes;
    private Long usedCloudBytes;
    private Long quotaLocalBytes;
    private Long quotaCloudBytes;
    private Long maxFileLocalBytes;
    private Long maxFileCloudBytes;
}
