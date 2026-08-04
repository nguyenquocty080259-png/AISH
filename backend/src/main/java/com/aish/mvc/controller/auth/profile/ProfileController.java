package com.aish.mvc.controller.auth.profile;

import com.aish.mvc.dto.auth.OnboardingRequest;
import com.aish.mvc.dto.auth.ProfileResponse;
import com.aish.mvc.dto.auth.UpdateProfileRequest;
import com.aish.mvc.service.auth.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * CỬA NGÕ API cho hồ sơ cá nhân (/api/profile): xem, cập nhật, onboarding lần đầu, đổi ảnh đại
 * diện. Toàn bộ đều thao tác trên user đang đăng nhập (lấy từ token), giao nghiệp vụ cho
 * {@link ProfileService}.
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // GET /api/profile/me — lấy hồ sơ của user đang đăng nhập.
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {

        return ResponseEntity.ok(
                profileService.getMyProfile()
        );
    }

    // PUT /api/profile/me — cập nhật toàn bộ hồ sơ.
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @Valid
            @RequestBody UpdateProfileRequest request
    ) {

        return ResponseEntity.ok(
                profileService.updateMyProfile(request)
        );
    }

    // POST /api/profile/avatar — tải ảnh đại diện mới lên.
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {

        String avatarUrl = profileService.uploadAvatar(file);

        return ResponseEntity.ok(
                Map.of("avatarUrl", avatarUrl)
        );
    }

    // PUT /api/profile/onboarding — hoàn tất thiết lập hồ sơ lần đầu sau khi đăng ký/đăng nhập OAuth.
    @PutMapping("/onboarding")
    public ResponseEntity<ProfileResponse> completeOnboarding(
            @Valid @RequestBody OnboardingRequest request
    ) {

        return ResponseEntity.ok(
                profileService.completeOnboarding(request)
        );
    }
}