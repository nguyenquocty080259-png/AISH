package com.aish.mvc.service.auth;

import com.aish.mvc.dto.auth.AuthResponse;
import com.aish.mvc.dto.auth.LoginRequest;
import com.aish.mvc.dto.auth.SignupRequest;
import com.aish.mvc.dto.auth.VerifyOtpRequest;

public interface  AuthService {

    void signup(SignupRequest request);

    AuthResponse login(LoginRequest request);

    void verifyOtp(VerifyOtpRequest request);

    void resendOtp(String email);

    void logout(String accessToken);

    void forgotPassword(String email);

    void verifyForgotPasswordOtp(
            VerifyOtpRequest request
    );

    void resetPassword(
            String email,
            String password
    );
}
