package com.aish.mvc.security;

import com.aish.mvc.entity.auth.*;
import com.aish.mvc.entity.enums.AuthProviders;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.repository.auth.*;
import com.aish.mvc.service.auth.JwtUtil;
import com.aish.mvc.service.auth.UsernameGenerator;
import com.aish.mvc.util.AuthProviderMessageUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final JwtUtil jwtUtil;
    private final UsernameGenerator usernameGenerator;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl = "http://localhost:5173";

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

        if (email != null) {
            email = email.trim().toLowerCase();
        }


        AuthProviders provider =
                AuthProviders.valueOf(
                        providerName.toUpperCase()
                );

        email = email.trim().toLowerCase();

        System.out.println("OAuth Email = " + email);

        Optional<AuthAccount> socialAccount =
                accountRepo.findByProviderAndIdentifier(
                        provider,
                        email
                );

        System.out.println("Found social account = " + socialAccount.isPresent());

        // Tìm account đúng provider
        Optional<AuthAccount> providerAccount =
                accountRepo.findByProviderAndIdentifier(
                        provider,
                        email
                );

        if (providerAccount.isPresent()) {

            AuthUser existingUser = providerAccount.get().getUser();

            if (existingUser.getStatus() == UserStatus.BANNED) {
                response.sendRedirect(frontendBaseUrl + "/login?error=banned");
                return;
            }

            if (existingUser.getStatus() == UserStatus.PENDING) {
                response.sendRedirect(frontendBaseUrl + "/login?error=pending");
                return;
            }

            String jwt = jwtUtil.generateToken(
                    email,
                    existingUser.getRole().getRoleName()
            );

            response.sendRedirect(
                    frontendBaseUrl + "/oauth-success?token=" + jwt
            );

            return;
        }

        // Email đã tồn tại nhưng bằng provider khác
        Optional<AuthAccount> existingAccount =
                accountRepo.findByIdentifier(email);

        if (existingAccount.isPresent()) {

            String providerMessage = AuthProviderMessageUtil.getProviderMessage(
                    existingAccount.get().getProvider()
            );

            response.sendRedirect(
                    frontendBaseUrl +
                            "/login?error=" +
                            URLEncoder.encode(providerMessage, StandardCharsets.UTF_8)
            );
            return;
        }

        // TẠO USER MỚI

        AuthUser user = new AuthUser();

        user.setStatus(UserStatus.ACTIVE);

        applyProviderProfile(user, provider, oauthUser);

        AuthRole role =
                roleRepo.findByRoleName("USER")
                        .orElseThrow();

        user.setRole(role);

        userRepo.save(user);

        usernameGenerator.createProfileForUser(user);

        AuthAccount account =
                new AuthAccount();

        account.setUser(user);

        account.setProvider(provider);

        account.setIdentifier(email);

        account.setIsVerified(true);

        accountRepo.save(account);

        String jwt =
                jwtUtil.generateToken(email, role.getRoleName());

        response.sendRedirect(
                frontendBaseUrl + "/oauth-success?token="
                        + jwt
        );
    }

    // Lấy fullName/avatar từ attribute của provider TRƯỚC khi sinh username, để username dựa
    // trên tên thật thay vì rơi về fallback "user". Không bao giờ throw ra ngoài - OAuth login
    // vẫn phải thành công dù attribute provider trả về bất thường.
    private void applyProviderProfile(AuthUser user, AuthProviders provider, OAuth2User oauthUser) {
        try {
            switch (provider) {
                case GOOGLE -> {
                    user.setFullName(asNonBlankString(oauthUser.getAttribute("name")));
                    user.setAvatarUrl(asNonBlankString(oauthUser.getAttribute("picture")));
                }
                case GITHUB -> {
                    user.setFullName(asNonBlankString(oauthUser.getAttribute("name")));
                    user.setAvatarUrl(asNonBlankString(oauthUser.getAttribute("avatar_url")));
                }
                case LOCAL -> {
                    // OAuth2SuccessHandler chỉ xử lý luồng social - LOCAL không đi qua đây.
                }
            }
        } catch (Exception ex) {
            // Bất kỳ lỗi mapping attribute nào cũng không được chặn việc tạo user mới.
        }
    }

    private String asNonBlankString(Object value) {
        return (value instanceof String str && !str.isBlank()) ? str : null;
    }
}