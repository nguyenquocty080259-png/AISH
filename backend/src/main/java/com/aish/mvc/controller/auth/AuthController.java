package com.aish.mvc.controller.auth;

import com.aish.mvc.dto.auth.*;
import com.aish.mvc.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request){
        System.out.println("SIGNUP API CALLED");
        authService.signup(request);
        return ResponseEntity.ok("Register success");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request){
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

    @RestController
    @RequestMapping("/test")
    @RequiredArgsConstructor
    public class TestController {

        private final JavaMailSender mailSender;

        @GetMapping("/mail")
        public String testMail() {

            SimpleMailMessage mail = new SimpleMailMessage();

            mail.setTo("your_email@gmail.com");
            mail.setSubject("Test");
            mail.setText("Hello");

            mailSender.send(mail);

            return "OK";
        }
    }

}
