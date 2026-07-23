package com.aish.mvc.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// GitHub trả "email" = null cho user để email riêng tư. Không có bước bổ sung này thì
// OAuth2SuccessHandler luôn thấy email null -> redirect no_email, chặn đăng nhập GitHub
// của phần lớn user thật. Chỉ can thiệp cho provider github, google đi qua nguyên vẹn.
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String GITHUB_REGISTRATION_ID = "github";
    private static final String GITHUB_EMAILS_URL = "https://api.github.com/user/emails";

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;
    private final RestTemplate restTemplate;

    public CustomOAuth2UserService() {
        this(new DefaultOAuth2UserService(), new RestTemplate());
    }

    CustomOAuth2UserService(
            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate,
            RestTemplate restTemplate
    ) {
        this.delegate = delegate;
        this.restTemplate = restTemplate;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (!GITHUB_REGISTRATION_ID.equalsIgnoreCase(registrationId)) {
            return oAuth2User;
        }

        String email = oAuth2User.getAttribute("email");
        if (email != null && !email.isBlank()) {
            return oAuth2User;
        }

        String primaryVerifiedEmail = fetchPrimaryVerifiedGithubEmail(
                userRequest.getAccessToken().getTokenValue());
        if (primaryVerifiedEmail == null) {
            return oAuth2User;
        }

        Map<String, Object> attributes = new LinkedHashMap<>(oAuth2User.getAttributes());
        attributes.put("email", primaryVerifiedEmail);

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, userNameAttributeName);
    }

    private String fetchPrimaryVerifiedGithubEmail(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/vnd.github+json");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    GITHUB_EMAILS_URL,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });

            List<Map<String, Object>> emails = response.getBody();
            if (emails == null) {
                return null;
            }

            return emails.stream()
                    .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                    .filter(e -> Boolean.TRUE.equals(e.get("verified")))
                    .map(e -> (String) e.get("email"))
                    .findFirst()
                    .orElse(null);
        } catch (RestClientException ex) {
            return null;
        }
    }
}
