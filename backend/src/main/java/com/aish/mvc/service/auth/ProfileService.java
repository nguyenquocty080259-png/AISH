package com.aish.mvc.service.auth;

import com.aish.mvc.dto.auth.ProfileResponse;
import com.aish.mvc.dto.auth.UpdateProfileRequest;

import java.time.LocalDate;

public interface ProfileService {
    ProfileResponse getMyProfile();

    ProfileResponse updateMyProfile(UpdateProfileRequest request);

    ProfileResponse completeOnboarding(LocalDate dob);
}
