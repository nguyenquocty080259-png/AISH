package com.aish.mvc.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@(gmail|yahoo|outlook|hotmail|aish)\\.(local|com|vn)$",
            message = "Email theo đúng định dạng"
    )
    private String email;
}