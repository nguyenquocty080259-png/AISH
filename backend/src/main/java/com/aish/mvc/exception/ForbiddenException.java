package com.aish.mvc.exception;

// Ném khi user KHÔNG có quyền thực hiện hành động (không phải chủ sở hữu, không phải admin...) -> 403.
// Tách biệt với Spring Security's AccessDeniedException (dùng cho các luật security-filter-level).
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
