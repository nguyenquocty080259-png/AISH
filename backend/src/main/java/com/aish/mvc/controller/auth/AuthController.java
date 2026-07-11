package com.aish.mvc.controller.auth;

import com.aish.mvc.dto.auth.*;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.service.auth.AuthService;
import com.aish.mvc.service.auth.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.aish.mvc.dto.auth.ResetTokenResponse;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final AuthAccountRepository accountRepo;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("Register success");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid
            @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody ResendOtpRequest request) {
        authService.resendOtp(request.getEmail());
        return ResponseEntity.ok("OTP resent successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid
            @RequestBody
            ForgotPasswordRequest request
    ) {
        System.out.println("Email: " + request.getEmail());
        authService.forgotPassword(
                request.getEmail()
        );
        System.out.println("Forgot password done");
        return ResponseEntity.ok(
                "OTP sent"
        );
    }

    @PostMapping("/verify-forgot-password")
    public ResponseEntity<ResetTokenResponse> verifyForgotPassword(
            @RequestBody VerifyOtpRequest request
    ) {

        ResetTokenResponse response =
                authService.verifyForgotPasswordOtp(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid
            @RequestBody
            ResetPasswordRequest request
    ) {

        authService.resetPassword(
                request.getResetToken(),
                request.getPassword()
        );

        return ResponseEntity.ok(
                "Password changed"
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);
        AuthAccount account = accountRepo.findByIdentifier(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Map<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("fullName", account.getUser().getFullName());
        result.put("status", account.getUser().getStatus());
        // Cho phép frontend show/hide nav admin + route-guard trang admin.
        result.put("role", account.getUser().getRole().getRoleName());
        return ResponseEntity.ok(result);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request) {

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return ResponseEntity.badRequest()
                    .body("Token missing");
        }

        String token = authHeader.substring(7);

        authService.logout(token);

        return ResponseEntity.ok("Logout successful");
    }

}