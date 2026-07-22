package com.aish.mvc.service.auth.impl;

import com.aish.mvc.dto.auth.*;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthEmailVerification;
import com.aish.mvc.entity.auth.AuthRole;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.AuthProviders;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthVerificationRepository;
import com.aish.mvc.repository.auth.AuthRoleRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.service.auth.AuthService;
import com.aish.mvc.service.auth.EmailService;
import com.aish.mvc.service.auth.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository userRepo;
    private final AuthAccountRepository accountRepo;
    private final AuthRoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthVerificationRepository verificationRepo;
    private final EmailService emailService;

    @Override
    public void signup(SignupRequest request) {
        AuthAccount existingAccount = accountRepo.findByIdentifier(request.getEmail()).orElse(null);
        // Email đã tồn tại
        if (existingAccount != null) {
            if (Boolean.TRUE.equals(existingAccount.getIsVerified())) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
            String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
            AuthEmailVerification verification = new AuthEmailVerification();
            verification.setAuthAccount(existingAccount);
            verification.setVerificationCode(otp);
            verification.setAttemptCount(0);
            verification.setIsUsed(false);
            verification.setExpiresAt(Instant.now().plusSeconds(120));
            verificationRepo.save(verification);
            emailService.sendOtpEmail(request.getEmail(), otp);
            return;
        }
        // Email chưa tồn tại

        AuthUser user = new AuthUser();
        user.setFullName(request.getFullName());
        user.setStatus(UserStatus.PENDING);
        System.out.println(roleRepo.findAll());
        AuthRole role = roleRepo.findByRoleName("USER").orElseThrow(() -> new RuntimeException("Role USER not found"));
        user.setRole(role);
        userRepo.save(user);

        AuthAccount account = new AuthAccount();
        account.setUser(user);
        account.setProvider(AuthProviders.LOCAL);
        account.setIdentifier(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        } else {
            account.setPasswordHash(null);
        }
        accountRepo.save(account);

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        AuthEmailVerification verification = new AuthEmailVerification();
        verification.setAuthAccount(account);
        verification.setVerificationCode(otp);
        verification.setAttemptCount(0);
        verification.setIsUsed(false);
        verification.setExpiresAt(Instant.now().plusSeconds(120));
        verificationRepo.save(verification);

        try {
            emailService.sendOtpEmail(request.getEmail(), otp);
        } catch (Exception e) {
            log.error("Không thể gửi OTP tới email {}: {}", request.getEmail(), e.getMessage());

            throw new IllegalArgumentException(
                    "Địa chỉ Gmail không tồn tại hoặc không thể nhận email."
            );
        }
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        AuthAccount account = accountRepo.findByProviderAndIdentifier(AuthProviders.LOCAL, request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        if (!Boolean.TRUE.equals(account.getIsVerified())) {
            throw new IllegalArgumentException("Vui lòng xác minh email trước khi đăng nhập.");
        }
        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu không chính xác.");
        }
        AuthUser user = account.getUser();

        if (user.getStatus() == UserStatus.PENDING) {
            throw new IllegalArgumentException("Tài khoản của bạn đang chờ phê duyệt.");
        }

        if (user.getStatus() == UserStatus.BANNED) {
            throw new IllegalArgumentException("Tài khoản của bạn đã bị khóa.");
        }

        AuthRole role = user.getRole();
        System.out.println(role.getId());
        System.out.println(role.getRoleName());
        String accessToken = jwtUtil.generateToken(account.getIdentifier(),
                                                    role.getRoleName());
        String refreshToken = UUID.randomUUID().toString();
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .role(role.getRoleName())
                .status(user.getStatus().name())
                .build();
    }

    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        AuthAccount account = accountRepo.findByIdentifier(request.getEmail()).orElseThrow(() -> new RuntimeException("Email not found"));
        AuthEmailVerification verification = verificationRepo.findTopByAuthAccountOrderByCreatedAtDesc(account).orElseThrow(() -> new RuntimeException("OTP not found"));
        if (Boolean.TRUE.equals(verification.getIsUsed())) {
            throw new RuntimeException("OTP đã được sử dụng");
        }
        if (Instant.now().isAfter(verification.getExpiresAt())) {
            throw new RuntimeException("Mã OTP đã hết hạn");
        }
        if (!verification.getVerificationCode().equals(request.getOtp())) {
            throw new RuntimeException("OTP không hợp lệ");
        }
        verification.setIsUsed(true);
        verification.setVerifiedAt(Instant.now());
        verificationRepo.save(verification);
        account.setIsVerified(true);
        accountRepo.save(account);
        AuthUser user = account.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);
    }

    @Transactional
    @Override
    public void resendOtp(String email) {
        AuthAccount account = accountRepo.findByIdentifier(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy email."));

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        AuthEmailVerification verification = new AuthEmailVerification();
        verification.setAuthAccount(account);
        verification.setVerificationCode(otp);
        verification.setAttemptCount(0);
        verification.setIsUsed(false);
        verification.setExpiresAt(Instant.now().plusSeconds(120));

        verificationRepo.save(verification);

        try {
            emailService.sendOtpEmail(email, otp);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể gửi OTP đến địa chỉ email này.");
        }
    }

    @Override
    public void logout(String accessToken) {
        String email = jwtUtil.extractUsername(accessToken);
        System.out.println("User logout: " + email);
    }

    @Override
    public void forgotPassword(String email) {
        AuthAccount account = accountRepo.findByIdentifier(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Email không tồn tại"));

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        AuthEmailVerification verification = new AuthEmailVerification();
        verification.setAuthAccount(account);
        verification.setVerificationCode(otp);
        verification.setAttemptCount(0);
        verification.setIsUsed(false);
        verification.setExpiresAt(Instant.now().plusSeconds(120));
        verificationRepo.save(verification);
        emailService.sendOtpEmail(email, otp);
    }

    @Override
    public ResetTokenResponse verifyForgotPasswordOtp(VerifyOtpRequest request) {
        AuthAccount account = accountRepo.findByIdentifier(request.getEmail()).orElseThrow(() -> new RuntimeException("Email not found"));
        AuthEmailVerification verification = verificationRepo.findTopByAuthAccountOrderByCreatedAtDesc(account).orElseThrow(() -> new RuntimeException("OTP not found"));
        if (Boolean.TRUE.equals(verification.getIsUsed())) {
            throw new RuntimeException("OTP đã được sử dụng");
        }
        if (Instant.now().isAfter(verification.getExpiresAt())) {
            throw new RuntimeException("Mã OTP đã hết hạn");
        }
        if (!verification.getVerificationCode().equals(request.getOtp())) {
            throw new RuntimeException("OTP không hợp lệ");
        }

        verification.setVerifiedAt(Instant.now());
        verification.setIsUsed(true);
        verificationRepo.save(verification);
        accountRepo.save(account);
        AuthUser user = account.getUser();
        userRepo.save(user);

        String resetToken = UUID.randomUUID().toString();

        verification.setResetToken(resetToken);

        verification.setResetTokenExpiresAt(
                Instant.now().plusSeconds(600)
        );

        verificationRepo.save(verification);
        return new ResetTokenResponse(resetToken);
    }

    @Override
    public void resetPassword(String resetToken, String password) {

        AuthEmailVerification verification = verificationRepo
                .findByResetToken(resetToken)
                .orElseThrow(() ->
                        new IllegalArgumentException("Reset token không hợp lệ"));

        if (verification.getResetTokenExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Reset token đã hết hạn");
        }

        AuthAccount account = verification.getAuthAccount();

        account.setPasswordHash(passwordEncoder.encode(password));
        accountRepo.save(account);

        verification.setResetToken(null);
        verification.setResetTokenExpiresAt(null);
        verification.setIsUsed(true);
        verificationRepo.save(verification);
    }
}
