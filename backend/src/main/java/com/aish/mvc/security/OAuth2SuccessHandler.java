package com.aish.mvc.security;

import com.aish.mvc.entity.auth.*;
import com.aish.mvc.entity.enums.AuthProviders;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.repository.auth.*;
import com.aish.mvc.service.auth.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler
        implements AuthenticationSuccessHandler {

    private final AuthAccountRepository accountRepo;
    private final AuthUserRepository userRepo;
    private final AuthRoleRepository roleRepo;
    private final AuthUserProfileRepository profileRepo;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2AuthenticationToken token =
                (OAuth2AuthenticationToken) authentication;

        String providerName =
                token.getAuthorizedClientRegistrationId();

        OAuth2User oauthUser =
                token.getPrincipal();

        String email = oauthUser.getAttribute("email");

        if(email == null || email.isBlank()) {

            response.sendRedirect(
                    "http://localhost:3000/login?error=no_email"
            );

            return;
        }

        AuthProviders provider =
                AuthProviders.valueOf(
                        providerName.toUpperCase()
                );

        Optional<AuthAccount> socialAccount =
                accountRepo.findByProviderAndIdentifier(
                        provider,
                        email
                );

        // LOGIN LẠI
        if(socialAccount.isPresent()) {

            String jwt =
                    jwtUtil.generateToken(email);

            response.sendRedirect(
                    "http://localhost:3000/oauth-success?token="
                            + jwt
            );

            return;
        }

        // EMAIL ĐÃ ĐĂNG KÝ LOCAL
        Optional<AuthAccount> localAccount =
                accountRepo.findByProviderAndIdentifier(
                        AuthProviders.LOCAL,
                        email
                );

        if(localAccount.isPresent()) {

            String msg =
                    URLEncoder.encode(
                            "Gmail của bạn đã được đăng ký. Hãy đăng nhập bằng Log In.",
                            StandardCharsets.UTF_8
                    );

            response.sendRedirect(
                    "http://localhost:3000/login?error="
                            + msg
            );

            return;
        }

        // TẠO USER MỚI

        AuthUser user = new AuthUser();

        user.setStatus(UserStatus.PENDING);

        user.setFullName(null);

        AuthRole role =
                roleRepo.findByRoleName("USER")
                        .orElseThrow();

        user.getAuthRoles().add(role);

        userRepo.save(user);

        AuthUserProfile profile =
                new AuthUserProfile();

        profile.setUser(user);

        profileRepo.save(profile);

        AuthAccount account =
                new AuthAccount();

        account.setUser(user);

        account.setProvider(provider);

        account.setIdentifier(email);

        account.setIsVerified(true);

        accountRepo.save(account);

        String jwt =
                jwtUtil.generateToken(email);

        response.sendRedirect(
                "http://localhost:3000/profile/setup?token="
                        + jwt
        );
    }
}