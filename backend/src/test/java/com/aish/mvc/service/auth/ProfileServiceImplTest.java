package com.aish.mvc.service.auth;

import com.aish.mvc.dto.auth.OnboardingRequest;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

        OnboardingRequest request = new OnboardingRequest();
        request.setDob(LocalDate.of(2000, 1, 1));

        ProfileResponse response = service.completeOnboarding(request);

        assertEquals(LocalDate.of(2000, 1, 1), response.getDob());
        assertEquals("Đã có bio từ trước", response.getBio());
        assertEquals("HCMUS", response.getUniversity());
        assertEquals("existinguser", response.getUsername());
    }

    // TRAP: onboarding gửi bio rỗng ("") không được xoá bio đã có từ trước - chỉ field khác
    // blank trong request mới ghi đè (partial-update), không giống updateMyProfile ghi đè toàn bộ.
    @Test
    void completeOnboardingBlankBioDoesNotEraseExistingBio() {
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
        existingProfile.setBio("Bio đã có từ trước");

        when(accountRepository.findByIdentifier("user@test.com")).thenReturn(Optional.of(account));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(AuthUserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        authenticateAs("user@test.com");
        ProfileServiceImpl service = new ProfileServiceImpl(
                accountRepository, profileRepository, userRepository, usernameGenerator);

        OnboardingRequest request = new OnboardingRequest();
        request.setDob(LocalDate.of(2001, 5, 5));
        request.setBio("");

        ProfileResponse response = service.completeOnboarding(request);

        assertEquals("Bio đã có từ trước", response.getBio());
    }

    // fullName trong request rỗng nhưng user ĐÃ có fullName từ trước (vd. đăng ký LOCAL, hoặc
    // Google OAuth đã map name) -> onboarding phải cho qua, không được bắt nhập lại.
    @Test
    void completeOnboardingBlankFullNameOkWhenUserAlreadyHasOne() {
        AuthAccountRepository accountRepository = mock(AuthAccountRepository.class);
        AuthUserProfileRepository profileRepository = mock(AuthUserProfileRepository.class);
        AuthUserRepository userRepository = mock(AuthUserRepository.class);
        UsernameGenerator usernameGenerator = mock(UsernameGenerator.class);

        AuthUser user = new AuthUser();
        user.setId(1L);
        user.setFullName("Đã Có Tên");

        AuthAccount account = new AuthAccount();
        account.setUser(user);

        AuthUserProfile existingProfile = new AuthUserProfile();
        existingProfile.setUser(user);
        existingProfile.setUsername("existinguser");

        when(accountRepository.findByIdentifier("user@test.com")).thenReturn(Optional.of(account));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(AuthUserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        authenticateAs("user@test.com");
        ProfileServiceImpl service = new ProfileServiceImpl(
                accountRepository, profileRepository, userRepository, usernameGenerator);

        OnboardingRequest request = new OnboardingRequest();
        request.setDob(LocalDate.of(2001, 5, 5));
        request.setFullName("  ");

        ProfileResponse response = service.completeOnboarding(request);

        assertEquals("Đã Có Tên", response.getFullName());
        verify(userRepository, never()).save(any());
    }

    // fullName rỗng và user CHƯA có fullName (vd. GitHub OAuth không trả name) -> phải bắt buộc
    // nhập, ném lỗi 400 tiếng Việt thay vì lưu một profile không tên.
    @Test
    void completeOnboardingBlankFullNameThrowsWhenUserHasNone() {
        AuthAccountRepository accountRepository = mock(AuthAccountRepository.class);
        AuthUserProfileRepository profileRepository = mock(AuthUserProfileRepository.class);
        AuthUserRepository userRepository = mock(AuthUserRepository.class);
        UsernameGenerator usernameGenerator = mock(UsernameGenerator.class);

        AuthUser user = new AuthUser();
        user.setId(1L);
        user.setFullName(null);

        AuthAccount account = new AuthAccount();
        account.setUser(user);

        AuthUserProfile existingProfile = new AuthUserProfile();
        existingProfile.setUser(user);
        existingProfile.setUsername("githubuser");

        when(accountRepository.findByIdentifier("user@test.com")).thenReturn(Optional.of(account));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));

        authenticateAs("user@test.com");
        ProfileServiceImpl service = new ProfileServiceImpl(
                accountRepository, profileRepository, userRepository, usernameGenerator);

        OnboardingRequest request = new OnboardingRequest();
        request.setDob(LocalDate.of(2001, 5, 5));

        assertThrows(IllegalArgumentException.class, () -> service.completeOnboarding(request));
        verify(profileRepository, never()).save(any());
    }

    // fullName được cung cấp trong request -> phải được set vào user và lưu lại.
    @Test
    void completeOnboardingSetsFullNameWhenProvided() {
        AuthAccountRepository accountRepository = mock(AuthAccountRepository.class);
        AuthUserProfileRepository profileRepository = mock(AuthUserProfileRepository.class);
        AuthUserRepository userRepository = mock(AuthUserRepository.class);
        UsernameGenerator usernameGenerator = mock(UsernameGenerator.class);

        AuthUser user = new AuthUser();
        user.setId(1L);
        user.setFullName(null);

        AuthAccount account = new AuthAccount();
        account.setUser(user);

        AuthUserProfile existingProfile = new AuthUserProfile();
        existingProfile.setUser(user);
        existingProfile.setUsername("githubuser");

        when(accountRepository.findByIdentifier("user@test.com")).thenReturn(Optional.of(account));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(AuthUserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        authenticateAs("user@test.com");
        ProfileServiceImpl service = new ProfileServiceImpl(
                accountRepository, profileRepository, userRepository, usernameGenerator);

        OnboardingRequest request = new OnboardingRequest();
        request.setDob(LocalDate.of(2001, 5, 5));
        request.setFullName("Nguyễn Văn A");

        ProfileResponse response = service.completeOnboarding(request);

        assertEquals("Nguyễn Văn A", response.getFullName());
        verify(userRepository).save(user);
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
