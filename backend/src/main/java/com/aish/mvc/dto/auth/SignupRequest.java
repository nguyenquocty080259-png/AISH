package com.aish.mvc.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    @Pattern(
            regexp = "^(?=.*[A-Z]).{8,}$",
            message = "Mật khẩu phải có ít nhất 1 chữ cái in hoa"
    )
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
//    @Pattern(
//            regexp = "^[^0-9]+$",
//            message = "Họ và tên không được chứa số"
//    )
    private String fullName;
}
