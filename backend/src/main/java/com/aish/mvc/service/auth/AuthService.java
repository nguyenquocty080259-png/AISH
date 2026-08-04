package com.aish.mvc.service.auth;

import com.aish.mvc.dto.auth.*;

/**
 * Nghiệp vụ AUTHENTICATION: đăng ký, đăng nhập, xác thực OTP qua email, quên/đặt lại mật khẩu,
 * đăng xuất. Đây là luồng tài khoản LOCAL (email + mật khẩu); đăng nhập Google/GitHub xử lý riêng
 * ở {@link com.aish.mvc.security.OAuth2SuccessHandler}.
 */
public interface  AuthService {

    // Đăng ký tài khoản mới bằng email + mật khẩu, gửi OTP xác minh qua email.
    void signup(SignupRequest request);

    // Đăng nhập bằng email + mật khẩu, trả về JWT (accessToken) nếu hợp lệ.
    AuthResponse login(LoginRequest request);

    // Xác minh OTP vừa gửi lúc đăng ký để kích hoạt tài khoản.
    void verifyOtp(VerifyOtpRequest request);

    // Gửi lại OTP mới (khi OTP cũ hết hạn hoặc không nhận được email).
    void resendOtp(String email);

    // Đăng xuất — hiện chỉ log lại, chưa có cơ chế thu hồi token (xem AuthServiceImpl).
    void logout(String accessToken);

    // Bắt đầu luồng quên mật khẩu: gửi OTP xác minh về email.
    void forgotPassword(String email);

    // Xác minh OTP quên mật khẩu, đổi lấy resetToken dùng một lần để đặt mật khẩu mới.
    ResetTokenResponse verifyForgotPasswordOtp(VerifyOtpRequest request);

    // Đặt mật khẩu mới bằng resetToken lấy được từ bước xác minh OTP ở trên.
    void resetPassword(
            String resetToken,
            String password
    );
}
