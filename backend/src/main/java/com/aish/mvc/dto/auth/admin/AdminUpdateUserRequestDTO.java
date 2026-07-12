package com.aish.mvc.dto.auth.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUpdateUserRequestDTO {

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    private String avatarUrl;

    @NotBlank(message = "Vai trò không được để trống")
    private String role;
}
