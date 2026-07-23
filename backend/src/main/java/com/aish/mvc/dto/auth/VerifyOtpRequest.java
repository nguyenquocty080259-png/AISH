package com.aish.mvc.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@(gmail|yahoo|outlook|hotmail|aish)\\.(local|com|vn)$",
            message = "Email theo đúng định dạng"
    )
    private String email;

    private String otp;
}
