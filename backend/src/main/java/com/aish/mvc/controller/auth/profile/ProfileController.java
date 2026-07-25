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

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {

        return ResponseEntity.ok(
                profileService.getMyProfile()
        );
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @Valid
            @RequestBody UpdateProfileRequest request
    ) {

        return ResponseEntity.ok(
                profileService.updateMyProfile(request)
        );
    }

    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {

        String avatarUrl = profileService.uploadAvatar(file);

        return ResponseEntity.ok(
                Map.of("avatarUrl", avatarUrl)
        );
    }

    @PutMapping("/onboarding")
    public ResponseEntity<ProfileResponse> completeOnboarding(
            @Valid @RequestBody OnboardingRequest request
    ) {

        return ResponseEntity.ok(
                profileService.completeOnboarding(request)
        );
    }
}