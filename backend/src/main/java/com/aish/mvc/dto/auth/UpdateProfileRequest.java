package com.aish.mvc.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateProfileRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private String bio;

    private LocalDate dob;

    private String gender;

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
