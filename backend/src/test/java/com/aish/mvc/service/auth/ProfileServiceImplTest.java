package com.aish.mvc.service.auth;

import com.aish.mvc.dto.auth.ProfileResponse;
import com.aish.mvc.dto.auth.UpdateProfileRequest;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.auth.AuthUserProfile;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserProfileRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileServiceImplTest {

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null));
    }

    // TRAP: completeOnboarding không được ghi đè các field khác (bio, university...) như
    // updateMyProfile vẫn làm - chỉ set dob rồi lưu lại đúng cùng entity đã có sẵn.
    @Test
    void completeOnboardingOnlySetsDobAndKeepsOtherFieldsIntact() {
        AuthAccountRepository accountRepository = mock(AuthAccountRepository.class);
        AuthUserProfileRepository profileRepository = mock(AuthUserProfileRepository.class);
        AuthUserRepository userRepository = mock(AuthUserRepository.class);
        UsernameGenerator usernameGenerator = mock(UsernameGenerator.class);

        AuthUser user = new AuthUser();
        user.setId(1L);
        user.setFullName("Existing User");

        AuthAccount account = new AuthAccount();
        account.setUser(user);

        AuthUserProfile existingProfile = new AuthUserProfile();
        existingProfile.setUser(user);
        existingProfile.setUsername("existinguser");
        existingProfile.setBio("Đã có bio từ trước");
        existingProfile.setUniversity("HCMUS");

        when(accountRepository.findByIdentifier("user@test.com")).thenReturn(Optional.of(account));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(AuthUserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        authenticateAs("user@test.com");
        ProfileServiceImpl service = new ProfileServiceImpl(
                accountRepository, profileRepository, userRepository, usernameGenerator);

        ProfileResponse response = service.completeOnboarding(LocalDate.of(2000, 1, 1));

        assertEquals(LocalDate.of(2000, 1, 1), response.getDob());
        assertEquals("Đã có bio từ trước", response.getBio());
        assertEquals("HCMUS", response.getUniversity());
        assertEquals("existinguser", response.getUsername());
    }

    @Test
    void updateMyProfileNeverWritesUsernameFromRequest() {
        AuthAccountRepository accountRepository = mock(AuthAccountRepository.class);
        AuthUserProfileRepository profileRepository = mock(AuthUserProfileRepository.class);
        AuthUserRepository userRepository = mock(AuthUserRepository.class);
        UsernameGenerator usernameGenerator = mock(UsernameGenerator.class);

        AuthUser user = new AuthUser();
        user.setId(1L);
        user.setFullName("Old Name");

        AuthAccount account = new AuthAccount();
        account.setUser(user);

        AuthUserProfile existingProfile = new AuthUserProfile();
        existingProfile.setUser(user);
        existingProfile.setUsername("systemgenerated");

        when(accountRepository.findByIdentifier("user@test.com")).thenReturn(Optional.of(account));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(AuthUserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        authenticateAs("user@test.com");
        ProfileServiceImpl service = new ProfileServiceImpl(
                accountRepository, profileRepository, userRepository, usernameGenerator);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("New Name");
        request.setBio("New bio");

        ProfileResponse response = service.updateMyProfile(request);

        ArgumentCaptor<AuthUserProfile> captor = ArgumentCaptor.forClass(AuthUserProfile.class);
        verify(profileRepository).save(captor.capture());
        assertEquals("systemgenerated", captor.getValue().getUsername());
        assertEquals("systemgenerated", response.getUsername());
    }
}
