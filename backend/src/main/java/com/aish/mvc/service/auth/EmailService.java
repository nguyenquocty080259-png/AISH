package com.aish.mvc.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Gửi email OTP (mã xác minh) — dùng cho đăng ký tài khoản và quên mật khẩu.
 * Chỉ soạn nội dung và gửi mail thuần (text), không lưu gì vào database.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Gửi email chứa mã OTP tới địa chỉ email. Đầu vào: email nhận + mã OTP. Không trả về gì.
    public void sendOtpEmail(
            String email,
            String otp
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);

        message.setSubject(
                "Email Verification"
        );

        message.setText(
                "Your OTP code is: "
                        + otp
                        + "\n\nThis code will expire in 1 minute."
        );

        mailSender.send(message);
    }
}