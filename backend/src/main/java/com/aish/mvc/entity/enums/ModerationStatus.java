package com.aish.mvc.entity.enums;

// Trạng thái kiểm duyệt cho tài liệu — dùng cho luồng chuyển sang PUBLIC (DEC-035).
public enum ModerationStatus {
    NOT_REQUIRED,  // Chưa từng thử public (đang PRIVATE/SHARED), chưa qua kiểm duyệt
    ADMIN_PENDING, // Chủ tài liệu đã yêu cầu công khai, AI đã pre-screen xong và đang CHỜ ADMIN
                   // duyệt cuối. Visibility vẫn PRIVATE cho tới khi Admin bấm duyệt — kết quả AI
                   // (PASS/FLAG) được lưu ở DocDocument.aiScreenOutcome để Admin biết ngữ cảnh.
    APPROVED,      // Admin đã duyệt: tài liệu công khai
    REJECTED       // Admin từ chối: tài liệu ở lại PRIVATE
}
