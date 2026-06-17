package com.aish.mvc.controller.auth;

import com.aish.mvc.dto.auth.*;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.service.auth.AuthService;
import com.aish.mvc.service.auth.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        System.out.println("SIGNUP API CALLED");
        authService.signup(request);
        return ResponseEntity.ok("Register success");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
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