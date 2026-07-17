package com.aish.mvc.service.auth;

import com.aish.mvc.dto.auth.ProfileResponse;
import com.aish.mvc.dto.auth.UpdateProfileRequest;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.auth.AuthUserProfile;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserProfileRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final AuthAccountRepository authAccountRepository;
    private final AuthUserProfileRepository authUserProfileRepository;
    private final AuthUserRepository authUserRepository;
    private final UsernameGenerator usernameGenerator;

    @Override
    public ProfileResponse getMyProfile() {

        String email = getCurrentEmail();

        AuthAccount account = authAccountRepository.findByIdentifier(email).orElseThrow(() -> new RuntimeException("Account not found"));

        AuthUser user = account.getUser();

        AuthUserProfile profile = authUserProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> usernameGenerator.createProfileForUser(user));

        return mapToResponse(user, profile);
    }

    @Override
    public ProfileResponse updateMyProfile(UpdateProfileRequest request) {

        String email = getCurrentEmail();

        AuthAccount account = authAccountRepository
                .findByIdentifier(email)
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        AuthUser user = account.getUser();

        AuthUserProfile profile = authUserProfileRepository
                .findByUserId(user.getId())
                .orElseGet(() -> usernameGenerator.createProfileForUser(user));
        // ==========================
        // VALIDATE trashRetentionDays: null cho phép (dùng mặc định 30), ngoài ra phải 1..90
        // ==========================
        if (request.getTrashRetentionDays() != null
                && (request.getTrashRetentionDays() < 1 || request.getTrashRetentionDays() > 90)) {
            throw new IllegalArgumentException(
                    "Số ngày giữ tài liệu trong thùng rác phải từ 1 đến 90.");
        }
        // AuthUser
        user.setFullName(request.getFullName());
        authUserRepository.save(user);
        // AuthUserProfile (username không nhận từ request nữa - do hệ thống tự sinh)
        profile.setBio(request.getBio());

        profile.setDob(request.getDob());

        profile.setGender(request.getGender());

        profile.setPhoneNumber(request.getPhoneNumber());

        profile.setUniversity(request.getUniversity());

        profile.setFaculty(request.getFaculty());

        profile.setMajor(request.getMajor());

        profile.setCountry(request.getCountry());

        profile.setCity(request.getCity());

        profile.setGithubUrl(request.getGithubUrl());

        profile.setLinkedinUrl(request.getLinkedinUrl());

        profile.setWebsiteUrl(request.getWebsiteUrl());

        profile.setTrashRetentionDays(request.getTrashRetentionDays());

        authUserProfileRepository.save(profile);

        return mapToResponse(user, profile);
    }

    // Onboarding chỉ set dob, KHÔNG động vào các field khác của profile -
    // updateMyProfile ghi đè toàn bộ nên không thể tái sử dụng cho bước này
    // (sẽ null hoá bio, university... của những field FE onboarding không gửi).
    @Override
    public ProfileResponse completeOnboarding(LocalDate dob) {

        String email = getCurrentEmail();

        AuthAccount account = authAccountRepository
                .findByIdentifier(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        AuthUser user = account.getUser();

        AuthUserProfile profile = authUserProfileRepository
                .findByUserId(user.getId())
                .orElseGet(() -> usernameGenerator.createProfileForUser(user));

        profile.setDob(dob);

        authUserProfileRepository.save(profile);

        return mapToResponse(user, profile);
    }

    private String getCurrentEmail() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication.getName();
    }

    private ProfileResponse mapToResponse(
            AuthUser user,
            AuthUserProfile profile
    ) {

        ProfileResponse response = new ProfileResponse();

        response.setUserId(user.getId());

        response.setFullName(user.getFullName());

        response.setAvatarUrl(user.getAvatarUrl());

        response.setUsername(profile.getUsername());

        response.setBio(profile.getBio());

        response.setDob(profile.getDob());

        response.setGender(profile.getGender());

        response.setPhoneNumber(profile.getPhoneNumber());

        response.setUniversity(profile.getUniversity());

        response.setFaculty(profile.getFaculty());

        response.setMajor(profile.getMajor());

        response.setCountry(profile.getCountry());

        response.setCity(profile.getCity());

        response.setGithubUrl(profile.getGithubUrl());

        response.setLinkedinUrl(profile.getLinkedinUrl());

        response.setWebsiteUrl(profile.getWebsiteUrl());

        response.setTrashRetentionDays(profile.getTrashRetentionDays());

        return response;
    }
}