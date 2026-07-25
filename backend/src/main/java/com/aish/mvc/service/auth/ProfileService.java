package com.aish.mvc.service.auth;

import com.aish.mvc.dto.auth.OnboardingRequest;
import com.aish.mvc.dto.auth.ProfileResponse;
import com.aish.mvc.dto.auth.UpdateProfileRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    ProfileResponse getMyProfile();

    ProfileResponse updateMyProfile(UpdateProfileRequest request);

    ProfileResponse completeOnboarding(OnboardingRequest request);

    String uploadAvatar(MultipartFile file);
}
