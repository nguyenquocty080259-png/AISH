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
    // Danh sách user được mời — chỉ dùng cho mode RESTRICTED.
    private List<Long> userIds;
    // V1 chỉ chấp nhận VIEWER hoặc COMMENTER (EDITOR để dành V2).
    private SharePermission permission;
}
