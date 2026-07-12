package com.aish.mvc.dto.auth.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUpdateUserStatusRequestDTO {

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;
}
