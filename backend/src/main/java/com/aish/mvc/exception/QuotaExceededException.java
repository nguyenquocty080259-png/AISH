package com.aish.mvc.exception;

// Ném khi user đã vượt quota (lượt hỏi/token chat trong ngày...) -> 429.
public class QuotaExceededException extends RuntimeException {
    public QuotaExceededException(String message) {
        super(message);
    }
}
