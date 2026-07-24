package com.aish.mvc.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateProfileRequest {
    @NotBlank(message = "Họ và tên không được để trống")
    @Pattern(
            regexp = "^[^0-9]+$",
            message = "Họ và tên không được chứa số"
    )
    private String fullName;

    private String avatarUrl;

    private String bio;
    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
    private LocalDate dob;

    private String gender;

    @Pattern(
            regexp = "^$|^(0?)(3[2-9]|5[689]|7[06-9]|8[0-689]|9[0-469])[0-9]{7}$",
            message = "Số điện thoại phải đúng định dạng."
    )
    private String phoneNumber;

    private String university;

    private String faculty;

    private String major;

    private String country;

    private String city;

    private String githubUrl;

    private String linkedinUrl;

    private String websiteUrl;

    private Integer trashRetentionDays;
}
