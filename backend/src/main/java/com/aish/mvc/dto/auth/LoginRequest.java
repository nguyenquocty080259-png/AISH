package com.aish.mvc.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@(gmail|yahoo|outlook|hotmail|aish)\\.(local|com|vn)$",
            message = "Email theo đúng định dạng"
    )
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    @Pattern(
            regexp = "^(?=.*[A-Z]).{8,}$",
            message = "Mật khẩu phải có ít nhất 1 chữ cái in hoa"
    )
    private String password;
}
