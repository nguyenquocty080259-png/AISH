package com.aish.mvc.exception;

// Ném khi tìm 1 tài nguyên (tài liệu, user...) theo id nhưng không tồn tại -> 404.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
