package com.aish.mvc.dto.auth;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String email;

    private String otp;
}
