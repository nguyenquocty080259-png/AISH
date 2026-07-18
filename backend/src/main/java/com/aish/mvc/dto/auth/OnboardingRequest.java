package com.aish.mvc.dto.auth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class OnboardingRequest {

    // Bắt buộc: nếu user chưa có fullName (vd. GitHub OAuth không trả name), phải nhập ở đây -
    // validate ở ProfileServiceImpl vì phụ thuộc dữ liệu hiện có của user, không chỉ payload.
    private String fullName;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
    private LocalDate dob;

    // Tất cả các field dưới đây là tùy chọn - có thể bỏ qua ở bước onboarding và bổ sung
    // sau tại trang hồ sơ. Chỉ giá trị khác blank mới được áp dụng (partial-update).
    private String bio;

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
}
