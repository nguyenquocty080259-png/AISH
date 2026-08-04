package com.aish.mvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Khai báo bean mã hoá mật khẩu dùng chung toàn hệ thống (BCrypt) — tách riêng khỏi
 * SecurityConfig để tránh phụ thuộc vòng khi các service khác cần inject PasswordEncoder.
 */
@Configuration
public class SecurityBeansConfig {

    // BCrypt: thuật toán băm một chiều có "muối" (salt) ngẫu nhiên, chuẩn cho lưu mật khẩu.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}