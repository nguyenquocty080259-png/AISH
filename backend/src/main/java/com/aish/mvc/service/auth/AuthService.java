package com.aish.mvc.service.auth;

import com.aish.mvc.dto.auth.AuthResponse;
import com.aish.mvc.dto.auth.LoginRequest;
import com.aish.mvc.dto.auth.SignupRequest;

public interface AuthService {

    void signup(SignupRequest request);

    AuthResponse login(LoginRequest request);
}
