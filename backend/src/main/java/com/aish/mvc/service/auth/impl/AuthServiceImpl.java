package com.aish.mvc.service.auth.impl;

import com.aish.mvc.dto.auth.AuthResponse;
import com.aish.mvc.dto.auth.LoginRequest;
import com.aish.mvc.dto.auth.SignupRequest;
import com.aish.mvc.dto.auth.VerifyOtpRequest;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthEmailVerification;
import com.aish.mvc.entity.auth.AuthRole;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.AuthProviders;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthEmailVerificationRepository;
import com.aish.mvc.repository.auth.AuthRoleRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.service.auth.AuthService;
import com.aish.mvc.service.auth.EmailService;
import com.aish.mvc.service.auth.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository userRepo;
    private final AuthAccountRepository accountRepo;
    private final AuthRoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthEmailVerificationRepository emailVerificationRepo;
    private final EmailService emailService;

    @Override
    public void signup(SignupRequest request) {
        AuthAccount existingAccount = accountRepo.findByIdentifier(request.getEmail()).orElse(null);
        // Email đã tồn tại
        if (existingAccount != null) {
            if (Boolean.TRUE.equals(existingAccount.getIsVerified())) {
                throw new RuntimeException("Email already exists");
            }
            String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
            AuthEmailVerification verification = new AuthEmailVerification();
            verification.setAuthAccount(existingAccount);
            verification.setVerificationCode(otp);
            verification.setAttemptCount(0);
            verification.setIsUsed(false);
            verification.setExpiresAt(Instant.now().plusSeconds(120));
            emailVerificationRepo.save(verification);
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
        emailVerificationRepo.save(verification);

        System.out.println("OTP = " + otp);

        try {
            emailService.sendOtpEmail(request.getEmail(), otp);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[DEV] Email failed, OTP = " + otp);
        }
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        AuthAccount account = accountRepo.findByProviderAndIdentifier(AuthProviders.LOCAL, request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!Boolean.TRUE.equals(account.getIsVerified())) {
            throw new RuntimeException("Please verify your email first");
        }
        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }
        AuthUser user = account.getUser();

        if (!account.getIsVerified()) {
            throw new RuntimeException("Email has not been verified.");
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw new RuntimeException("Your account is pending approval.");
        }

        if (user.getStatus() == UserStatus.BANNED) {
            throw new RuntimeException("Your account has been blocked.");
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
        AuthEmailVerification verification = emailVerificationRepo.findTopByAuthAccountOrderByCreatedAtDesc(account).orElseThrow(() -> new RuntimeException("OTP not found"));
        if (Boolean.TRUE.equals(verification.getIsUsed())) {
            throw new RuntimeException("OTP already used");
        }
        if (Instant.now().isAfter(verification.getExpiresAt())) {
            throw new RuntimeException("OTP expired");
        }
        if (!verification.getVerificationCode().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }
        verification.setIsUsed(true);
        verification.setVerifiedAt(Instant.now());
        emailVerificationRepo.save(verification);
        account.setIsVerified(true);
        accountRepo.save(account);
        AuthUser user = account.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);
    }

    @Override
    public void resendOtp(String email) {
        AuthAccount account = accountRepo.findByIdentifier(email).orElseThrow(() -> new RuntimeException("Email not found"));
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        AuthEmailVerification verification = new AuthEmailVerification();
        verification.setAuthAccount(account);
        verification.setVerificationCode(otp);
        verification.setAttemptCount(0);
        verification.setIsUsed(false);
        verification.setExpiresAt(Instant.now().plusSeconds(120));
        emailVerificationRepo.save(verification);
        try {
            emailService.sendOtpEmail(email, otp);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[DEV] Email failed, NEW OTP = " + otp);
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
                .orElseThrow(() -> new RuntimeException("Email not found"));
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        AuthEmailVerification verification = new AuthEmailVerification();
        verification.setAuthAccount(account);
        verification.setVerificationCode(otp);
        verification.setAttemptCount(0);
        verification.setIsUsed(false);
        verification.setExpiresAt(Instant.now().plusSeconds(120));
        emailVerificationRepo.save(verification);
        emailService.sendOtpEmail(email, otp);
    }

    @Override
    public void verifyForgotPasswordOtp(VerifyOtpRequest request) {
        AuthAccount account = accountRepo.findByIdentifier(request.getEmail()).orElseThrow(() -> new RuntimeException("Email not found"));
        AuthEmailVerification verification = emailVerificationRepo.findTopByAuthAccountOrderByCreatedAtDesc(account).orElseThrow(() -> new RuntimeException("OTP not found"));
        if (Boolean.TRUE.equals(verification.getIsUsed())) {
            throw new RuntimeException("OTP already used");
        }
        if (Instant.now().isAfter(verification.getExpiresAt())) {
            throw new RuntimeException("OTP expired");
        }
        if (!verification.getVerificationCode().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }
        verification.setIsUsed(true);
        verification.setVerifiedAt(Instant.now());
        emailVerificationRepo.save(verification);
        accountRepo.save(account);
        AuthUser user = account.getUser();
        userRepo.save(user);
    }

    @Override
    public void resetPassword(String email, String password) {
        AuthAccount account = accountRepo.findByIdentifier(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        account.setPasswordHash(passwordEncoder.encode(password));
        accountRepo.save(account);
    }
}
