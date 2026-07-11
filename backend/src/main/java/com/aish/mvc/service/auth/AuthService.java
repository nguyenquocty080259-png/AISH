package com.aish.mvc.service.auth;

import com.aish.mvc.dto.auth.*;

public interface  AuthService {

    void signup(SignupRequest request);

    AuthResponse login(LoginRequest request);

    void verifyOtp(VerifyOtpRequest request);

    void resendOtp(String email);

    void logout(String accessToken);

    void forgotPassword(String email);

    ResetTokenResponse verifyForgotPasswordOtp(VerifyOtpRequest request);

    void resetPassword(
            String resetToken,
            String password
    );
}
