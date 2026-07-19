package com.aish.mvc.security;

import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthRole;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.AuthProviders;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthRoleRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.service.auth.JwtUtil;
import com.aish.mvc.service.auth.UsernameGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuth2SuccessHandlerTest {

    private OAuth2AuthenticationToken tokenFor(String email, String registrationId) {
        return tokenFor(email, registrationId, Map.of());
    }

    private OAuth2AuthenticationToken tokenFor(
            String email, String registrationId, Map<String, Object> extraAttributes) {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        extraAttributes.forEach((key, value) ->
                when(oAuth2User.getAttribute(key)).thenReturn(value));
        return new OAuth2AuthenticationToken(oAuth2User, AuthorityUtils.NO_AUTHORITIES, registrationId);
    }

    private OAuth2SuccessHandler newHandlerForNewUser(
            AuthAccountRepository accountRepo,
            AuthUserRepository userRepo,
            AuthRoleRepository roleRepo,
            JwtUtil jwtUtil,
            UsernameGenerator usernameGenerator,
            String email,
            AuthProviders provider) {
        when(accountRepo.findByProviderAndIdentifier(provider, email)).thenReturn(Optional.empty());
        when(accountRepo.findByProviderAndIdentifier(AuthProviders.LOCAL, email)).thenReturn(Optional.empty());
        AuthRole userRole = new AuthRole();
        userRole.setRoleName("USER");
        when(roleRepo.findByRoleName("USER")).thenReturn(Optional.of(userRole));
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt");
        return new OAuth2SuccessHandler(accountRepo, userRepo, roleRepo, jwtUtil, usernameGenerator);
    }

    @Test
    void newUserHappyPathCreatesUserAndRedirectsWithTokenFromGenerateToken() throws Exception {
        AuthAccountRepository accountRepo = mock(AuthAccountRepository.class);
        AuthUserRepository userRepo = mock(AuthUserRepository.class);
        AuthRoleRepository roleRepo = mock(AuthRoleRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        UsernameGenerator usernameGenerator = mock(UsernameGenerator.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String email = "new.social.user@example.com";
        AuthRole userRole = new AuthRole();
        userRole.setRoleName("USER");

        when(accountRepo.findByProviderAndIdentifier(AuthProviders.GOOGLE, email)).thenReturn(Optional.empty());
        when(accountRepo.findByProviderAndIdentifier(AuthProviders.LOCAL, email)).thenReturn(Optional.empty());
        when(roleRepo.findByRoleName("USER")).thenReturn(Optional.of(userRole));
        when(jwtUtil.generateToken(email, "USER")).thenReturn("new-user-jwt");

        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(accountRepo, userRepo, roleRepo, jwtUtil, usernameGenerator);

        handler.onAuthenticationSuccess(request, response, tokenFor(email, "google"));

        ArgumentCaptor<AuthUser> userCaptor = ArgumentCaptor.forClass(AuthUser.class);
        verify(userRepo).save(userCaptor.capture());
        assertEquals(UserStatus.ACTIVE, userCaptor.getValue().getStatus());
        verify(usernameGenerator).createProfileForUser(userCaptor.getValue());

        ArgumentCaptor<AuthAccount> accountCaptor = ArgumentCaptor.forClass(AuthAccount.class);
        verify(accountRepo).save(accountCaptor.capture());
        assertEquals(AuthProviders.GOOGLE, accountCaptor.getValue().getProvider());
        assertEquals(email, accountCaptor.getValue().getIdentifier());
        assertEquals(Boolean.TRUE, accountCaptor.getValue().getIsVerified());

        verify(response).sendRedirect("http://localhost:5173/oauth-success?token=new-user-jwt");
    }

    @Test
    void bannedUserReloginRedirectsToBannedErrorAndNeverIssuesToken() throws Exception {
        AuthAccountRepository accountRepo = mock(AuthAccountRepository.class);
        AuthUserRepository userRepo = mock(AuthUserRepository.class);
        AuthRoleRepository roleRepo = mock(AuthRoleRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        UsernameGenerator usernameGenerator = mock(UsernameGenerator.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String email = "banned.social.user@example.com";
        AuthUser bannedUser = new AuthUser();
        bannedUser.setStatus(UserStatus.BANNED);
        AuthRole role = new AuthRole();
        role.setRoleName("USER");
        bannedUser.setRole(role);

        AuthAccount existingAccount = new AuthAccount();
        existingAccount.setUser(bannedUser);
        existingAccount.setProvider(AuthProviders.GOOGLE);
        existingAccount.setIdentifier(email);

        when(accountRepo.findByProviderAndIdentifier(AuthProviders.GOOGLE, email))
                .thenReturn(Optional.of(existingAccount));

        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(accountRepo, userRepo, roleRepo, jwtUtil, usernameGenerator);

        handler.onAuthenticationSuccess(request, response, tokenFor(email, "google"));

        verify(response).sendRedirect("http://localhost:5173/login?error=banned");
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void pendingUserReloginRedirectsToPendingErrorAndNeverIssuesToken() throws Exception {
        AuthAccountRepository accountRepo = mock(AuthAccountRepository.class);
        AuthUserRepository userRepo = mock(AuthUserRepository.class);
        AuthRoleRepository roleRepo = mock(AuthRoleRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        UsernameGenerator usernameGenerator = mock(UsernameGenerator.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String email = "pending.social.user@example.com";
        AuthUser pendingUser = new AuthUser();
        pendingUser.setStatus(UserStatus.PENDING);
        AuthRole role = new AuthRole();
        role.setRoleName("USER");
        pendingUser.setRole(role);

        AuthAccount existingAccount = new AuthAccount();
        existingAccount.setUser(pendingUser);
        existingAccount.setProvider(AuthProviders.GITHUB);
        existingAccount.setIdentifier(email);

        when(accountRepo.findByProviderAndIdentifier(AuthProviders.GITHUB, email))
                .thenReturn(Optional.of(existingAccount));

        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(accountRepo, userRepo, roleRepo, jwtUtil, usernameGenerator);

        handler.onAuthenticationSuccess(request, response, tokenFor(email, "github"));

        verify(response).sendRedirect("http://localhost:5173/login?error=pending");
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void activeUserReloginIssuesTokenWithCorrectRole() throws Exception {
        AuthAccountRepository accountRepo = mock(AuthAccountRepository.class);
        AuthUserRepository userRepo = mock(AuthUserRepository.class);
        AuthRoleRepository roleRepo = mock(AuthRoleRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        UsernameGenerator usernameGenerator = mock(UsernameGenerator.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String email = "active.social.user@example.com";
        AuthUser activeUser = new AuthUser();
        activeUser.setStatus(UserStatus.ACTIVE);
        AuthRole role = new AuthRole();
        role.setRoleName("ADMIN");
        activeUser.setRole(role);

        AuthAccount existingAccount = new AuthAccount();
        existingAccount.setUser(activeUser);
        existingAccount.setProvider(AuthProviders.GOOGLE);
        existingAccount.setIdentifier(email);

        when(accountRepo.findByProviderAndIdentifier(AuthProviders.GOOGLE, email))
                .thenReturn(Optional.of(existingAccount));
        when(jwtUtil.generateToken(email, "ADMIN")).thenReturn("relogin-jwt");

        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(accountRepo, userRepo, roleRepo, jwtUtil, usernameGenerator);

        handler.onAuthenticationSuccess(request, response, tokenFor(email, "google"));

        verify(response).sendRedirect("http://localhost:5173/oauth-success?token=relogin-jwt");
        verify(response, never()).sendRedirect("http://localhost:5173/login?error=banned");
        verify(response, never()).sendRedirect("http://localhost:5173/login?error=pending");
    }

    @Test
    void noEmailAttributeRedirectsToNoEmailErrorAndCreatesNothing() throws Exception {
        AuthAccountRepository accountRepo = mock(AuthAccountRepository.class);
        AuthUserRepository userRepo = mock(AuthUserRepository.class);
        AuthRoleRepository roleRepo = mock(AuthRoleRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        UsernameGenerator usernameGenerator = mock(UsernameGenerator.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(accountRepo, userRepo, roleRepo, jwtUtil, usernameGenerator);

        handler.onAuthenticationSuccess(request, response, tokenFor(null, "github"));

        verify(response).sendRedirect("http://localhost:5173/login?error=no_email");
        verify(userRepo, never()).save(any());
    }

    @Test
    void googleNewUserMapsFullNameAndAvatarFromProviderAttributes() throws Exception {
        AuthAccountRepository accountRepo = mock(AuthAccountRepository.class);
        AuthUserRepository userRepo = mock(AuthUserRepository.class);
        AuthRoleRepository roleRepo = mock(AuthRoleRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        UsernameGenerator usernameGenerator = mock(UsernameGenerator.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String email = "google.new.user@example.com";
        OAuth2SuccessHandler handler = newHandlerForNewUser(
                accountRepo, userRepo, roleRepo, jwtUtil, usernameGenerator, email, AuthProviders.GOOGLE);

        handler.onAuthenticationSuccess(request, response, tokenFor(email, "google", Map.of(
                "name", "Nguyen Van A",
                "picture", "https://lh3.googleusercontent.com/a/avatar.jpg"
        )));

        ArgumentCaptor<AuthUser> userCaptor = ArgumentCaptor.forClass(AuthUser.class);
        verify(userRepo).save(userCaptor.capture());
        assertEquals("Nguyen Van A", userCaptor.getValue().getFullName());
        assertEquals("https://lh3.googleusercontent.com/a/avatar.jpg", userCaptor.getValue().getAvatarUrl());
        verify(usernameGenerator).createProfileForUser(userCaptor.getValue());
    }

    @Test
    void githubNewUserWithNullNameLeavesFullNameNullButMapsAvatar() throws Exception {
        AuthAccountRepository accountRepo = mock(AuthAccountRepository.class);
        AuthUserRepository userRepo = mock(AuthUserRepository.class);
        AuthRoleRepository roleRepo = mock(AuthRoleRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        UsernameGenerator usernameGenerator = mock(UsernameGenerator.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String email = "github.new.user@example.com";
        OAuth2SuccessHandler handler = newHandlerForNewUser(
                accountRepo, userRepo, roleRepo, jwtUtil, usernameGenerator, email, AuthProviders.GITHUB);

        handler.onAuthenticationSuccess(request, response, tokenFor(email, "github", Map.of(
                "avatar_url", "https://avatars.githubusercontent.com/u/1"
        )));

        ArgumentCaptor<AuthUser> userCaptor = ArgumentCaptor.forClass(AuthUser.class);
        verify(userRepo).save(userCaptor.capture());
        assertNull(userCaptor.getValue().getFullName());
        assertEquals("https://avatars.githubusercontent.com/u/1", userCaptor.getValue().getAvatarUrl());
    }

}
