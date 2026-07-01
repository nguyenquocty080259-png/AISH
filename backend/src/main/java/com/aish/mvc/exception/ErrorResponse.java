package com.aish.mvc.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// Hình dạng lỗi JSON thống nhất toàn app: { timestamp, status, error, message, path }.
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
