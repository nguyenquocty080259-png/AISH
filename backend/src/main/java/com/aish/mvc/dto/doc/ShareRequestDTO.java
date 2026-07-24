package com.aish.mvc.dto.doc;

import com.aish.mvc.entity.enums.SharePermission;
import com.aish.mvc.entity.enums.ShareMode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ShareRequestDTO {
    private ShareMode mode;
    // Chia sẻ theo EMAIL (luồng chính của modal V1) — mode RESTRICTED. BE trim + so khớp
    // không phân biệt hoa/thường rồi resolve ra user đã có tài khoản HiveMind.
    private String email;
    // Danh sách user được mời theo ID — giữ để tương thích ngược; chỉ dùng cho mode RESTRICTED.
    private List<Long> userIds;
    // V1 chỉ chấp nhận VIEWER hoặc COMMENTER (EDITOR để dành V2).
    private SharePermission permission;
}
