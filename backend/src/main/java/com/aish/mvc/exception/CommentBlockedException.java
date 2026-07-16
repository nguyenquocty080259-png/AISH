package com.aish.mvc.exception;

import lombok.Getter;

@Getter
public class CommentBlockedException extends RuntimeException {

    private final String reason;

    public CommentBlockedException(String reason) {
        super(reason);
        this.reason = reason;
    }
}
