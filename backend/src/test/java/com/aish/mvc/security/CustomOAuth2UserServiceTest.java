package com.aish.mvc.security;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomOAuth2UserServiceTest {

    @SuppressWarnings("unchecked")
    private OAuth2UserRequest githubRequest(String accessToken) {
        ClientRegistration registration = ClientRegistration.withRegistrationId("github")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("id")
                .build();
        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, accessToken, null, null);
        return new OAuth2UserRequest(registration, token);
    }

    private OAuth2User githubUserWithAttributes(Map<String, Object> attributes) {
        return new DefaultOAuth2User(java.util.List.of(), attributes, "id");
    }

    @Test
    void resolvesPrimaryVerifiedEmailWhenGithubEmailIsPrivate() {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = mock(OAuth2UserService.class);
        RestTemplate restTemplate = mock(RestTemplate.class);

        OAuth2User userWithoutEmail = githubUserWithAttributes(Map.of("id", 42, "login", "octocat"));
        OAuth2UserRequest request = githubRequest("gh-access-token");
        when(delegate.loadUser(request)).thenReturn(userWithoutEmail);

        List<Map<String, Object>> emails = List.of(
                Map.of("email", "secondary@example.com", "primary", false, "verified", true),
                Map.of("email", "primary@example.com", "primary", true, "verified", true));
        when(restTemplate.exchange(
                eq("https://api.github.com/user/emails"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(emails));

        CustomOAuth2UserService service = new CustomOAuth2UserService(delegate, restTemplate);

        OAuth2User result = service.loadUser(request);

        assertEquals("primary@example.com", result.getAttribute("email"));
        assertEquals(42, (Integer) result.getAttribute("id"));
    }

    @Test
    void noVerifiedPrimaryEmailLeavesEmailAttributeAbsentForNoEmailFallback() {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = mock(OAuth2UserService.class);
        RestTemplate restTemplate = mock(RestTemplate.class);

        OAuth2User userWithoutEmail = githubUserWithAttributes(Map.of("id", 7, "login", "no-email-user"));
        OAuth2UserRequest request = githubRequest("gh-access-token");
        when(delegate.loadUser(request)).thenReturn(userWithoutEmail);

        List<Map<String, Object>> emails = List.of(
                Map.of("email", "unverified@example.com", "primary", true, "verified", false));
        when(restTemplate.exchange(
                eq("https://api.github.com/user/emails"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(emails));

        CustomOAuth2UserService service = new CustomOAuth2UserService(delegate, restTemplate);

        OAuth2User result = service.loadUser(request);

        assertNull(result.getAttribute("email"));
    }

    @Test
    void nonGithubProviderPassesThroughWithoutCallingGithubApi() {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = mock(OAuth2UserService.class);
        RestTemplate restTemplate = mock(RestTemplate.class);

        ClientRegistration googleRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                .userNameAttributeName("sub")
                .build();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "google-token", null, null);
        OAuth2UserRequest request = new OAuth2UserRequest(googleRegistration, accessToken);

        OAuth2User googleUser = new DefaultOAuth2User(
                java.util.List.of(), Map.of("sub", "1", "email", "already@present.com"), "sub");
        when(delegate.loadUser(request)).thenReturn(googleUser);

        CustomOAuth2UserService service = new CustomOAuth2UserService(delegate, restTemplate);

        OAuth2User result = service.loadUser(request);

        assertEquals("already@present.com", result.getAttribute("email"));
        verify(restTemplate, never()).exchange(
                any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }
}
