package com.aish.mvc.entity.enums;

// Trạng thái kiểm duyệt AI cho tài liệu — dùng cho luồng chuyển sang PUBLIC (DEC-035).
public enum ModerationStatus {
    NOT_REQUIRED, // Chưa từng thử public (đang PRIVATE/SHARED), chưa qua kiểm duyệt
    PENDING,      // AI FLAG -> đang chờ Admin duyệt thủ công (Admin flagged queue)
    APPROVED,     // AI PASS tự động, hoặc Admin duyệt thủ công sau khi bị FLAG
    REJECTED      // Admin từ chối sau khi bị FLAG
}
