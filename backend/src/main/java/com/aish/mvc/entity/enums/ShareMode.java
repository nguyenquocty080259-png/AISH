package com.aish.mvc.entity.enums;

public enum ShareMode {
    RESTRICTED,        // Chỉ những người được mời (theo userId) mới xem được
    ANYONE_WITH_LINK,  // Bất kỳ ai có link (token) đều xem được
    NONE               // Không chia sẻ qua link (tắt link-share)
}
