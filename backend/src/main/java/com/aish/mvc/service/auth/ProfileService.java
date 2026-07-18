package com.aish.mvc.service.auth;

import com.aish.mvc.dto.auth.OnboardingRequest;
import com.aish.mvc.dto.auth.ProfileResponse;
import com.aish.mvc.dto.auth.UpdateProfileRequest;

public interface ProfileService {
    ProfileResponse getMyProfile();

    ProfileResponse updateMyProfile(UpdateProfileRequest request);

    ProfileResponse completeOnboarding(OnboardingRequest request);
}
