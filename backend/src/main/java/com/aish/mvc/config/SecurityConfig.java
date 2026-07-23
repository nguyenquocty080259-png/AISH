package com.aish.mvc.config;

import com.aish.mvc.exception.RestAccessDeniedHandler;
import com.aish.mvc.exception.RestAuthenticationEntryPoint;
import com.aish.mvc.security.CustomOAuth2UserService;
import com.aish.mvc.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
//                .sessionManagement(s ->
//                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/signup",
                                "/api/auth/verify-otp",
                                "/api/auth/resend-otp",
                                "/api/auth/forgot-password",
                                "/api/auth/verify-forgot-password",
                                "/api/auth/reset-password",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        )
                        .permitAll()

                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // DEC-030: Subject là admin-managed — chỉ GET (duyệt danh sách) mở cho
                        // mọi user đã đăng nhập, các thao tác ghi phải là ADMIN.
                        .requestMatchers(HttpMethod.POST, "/api/subjects/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/subjects/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/subjects/**").hasRole("ADMIN")

                        .requestMatchers("/api/user/**")
                        .hasAnyRole("USER","ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {

                            exception.printStackTrace();

                            response.sendRedirect(
                                    "http://localhost:5173/login?error=oauth_failed"
                            );
                        })
                )
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }
}
