package com.aish.mvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("===== CUSTOM SECURITY CONFIG LOADED =====");
        http
                .csrf(csrf -> csrf.disable())
                // Ép Spring Security phải tôn trọng cấu hình CORS hệ thống
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        // MỞ CỬA CHO TÀI LIỆU:
                        .requestMatchers("/api/documents/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}