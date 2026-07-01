package com.aish.mvc.controller.auth.profile;

import com.aish.mvc.dto.auth.ProfileResponse;
import com.aish.mvc.dto.auth.UpdateProfileRequest;
import com.aish.mvc.service.auth.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody UpdateProfileRequest request
    ) {

        return ResponseEntity.ok(
                profileService.updateMyProfile(request)
        );
    }
}