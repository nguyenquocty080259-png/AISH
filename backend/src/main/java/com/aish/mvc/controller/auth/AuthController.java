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

/**
 * CỬA NGÕ API của luồng đăng nhập/đăng ký: đăng ký, đăng nhập, xác minh OTP, quên/đặt lại mật
 * khẩu, lấy thông tin bản thân (/me), đăng xuất. Chỉ nhận request và gọi {@link AuthService};
 * nghiệp vụ và kiểm tra hợp lệ nằm ở tầng service.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final AuthAccountRepository accountRepo;

    // POST /api/auth/signup — đăng ký tài khoản mới, gửi OTP xác minh về email.
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        System.out.println("===== SIGNUP CONTROLLER =====");
        authService.signup(request);
        return ResponseEntity.ok("Register thành công");
    }

    // POST /api/auth/login — đăng nhập bằng email + mật khẩu, trả về JWT.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid
            @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // POST /api/auth/verify-otp — xác minh OTP đăng ký để kích hoạt tài khoản.
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok("Email đã xác thực thành công");
    }

    // POST /api/auth/resend-otp — gửi lại OTP mới.
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody ResendOtpRequest request) {
        authService.resendOtp(request.getEmail());
        return ResponseEntity.ok("OTP resent successfully");
    }

    // POST /api/auth/forgot-password — bắt đầu luồng quên mật khẩu, gửi OTP về email.
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
        return ResponseEntity.ok(
                "OTP đã gửi"
        );
    }

    // POST /api/auth/verify-forgot-password — xác minh OTP quên mật khẩu, đổi lấy resetToken.
    @PostMapping("/verify-forgot-password")
    public ResponseEntity<ResetTokenResponse> verifyForgotPassword(
            @RequestBody VerifyOtpRequest request
    ) {

        ResetTokenResponse response =
                authService.verifyForgotPasswordOtp(request);

        return ResponseEntity.ok(response);
    }

    // POST /api/auth/reset-password — đặt mật khẩu mới bằng resetToken.
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
                "Password đã đổi"
        );
    }

    // GET /api/auth/me — đọc JWT trong header Authorization, trả về thông tin cơ bản của user
    // đang đăng nhập (email, họ tên, trạng thái, vai trò). FE dùng role để ẩn/hiện menu Admin.
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@Valid @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);
        AuthAccount account = accountRepo.findByIdentifier(email)
                .orElseThrow(() -> new RuntimeException("User không tìm thấy"));
        Map<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("fullName", account.getUser().getFullName());
        result.put("status", account.getUser().getStatus());
        // Cho phép frontend show/hide nav admin + route-guard trang admin.
        result.put("role", account.getUser().getRole().getRoleName());
        return ResponseEntity.ok(result);
    }
    // POST /api/auth/logout — đăng xuất (chỉ ghi log ở BE; FE tự xoá token đang lưu).
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

        return ResponseEntity.ok("Đã đăng xuất thành công ");
    }

}