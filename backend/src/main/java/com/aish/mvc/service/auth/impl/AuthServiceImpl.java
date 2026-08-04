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
import com.aish.mvc.util.AuthProviderMessageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.aish.mvc.entity.enums.AuthProvider.GITHUB;
import static com.aish.mvc.entity.enums.AuthProvider.GOOGLE;

/**
 * Cài đặt thật của {@link AuthService} — luồng đăng nhập/đăng ký bằng email + mật khẩu (LOCAL).
 * OTP có hạn 120 giây, resetToken (quên mật khẩu) có hạn 600 giây (10 phút). Mật khẩu luôn được
 * băm (hash) trước khi lưu, không bao giờ lưu plain text.
 */
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

    // Đăng ký tài khoản mới. Đầu vào: email, mật khẩu, họ tên. Không trả về gì (báo lỗi qua exception).
    // Các bước: (1) email đã có tài khoản VÀ đã xác minh -> báo lỗi trùng; (2) email đã có nhưng
    // CHƯA xác minh -> chỉ gửi lại OTP mới, không tạo tài khoản trùng; (3) email hoàn toàn mới ->
    // tạo AuthUser (trạng thái PENDING) + AuthAccount (LOCAL, mật khẩu đã băm) rồi gửi OTP xác minh.
    @Override
    public void signup(SignupRequest request) {
        AuthAccount existingAccount = accountRepo.findByIdentifier(request.getEmail()).orElse(null);
        // Email đã tồn tại
        if (existingAccount != null) {
            if (Boolean.TRUE.equals(existingAccount.getIsVerified())) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
            // Email tồn tại nhưng chưa xác minh -> coi như đăng ký lại: gửi OTP mới, không tạo user mới.
            String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
            AuthEmailVerification verification = new AuthEmailVerification();
            verification.setAuthAccount(existingAccount);
            verification.setVerificationCode(otp);
            verification.setAttemptCount(0);
            verification.setIsUsed(false);
            verification.setExpiresAt(Instant.now().plusSeconds(120)); // OTP hết hạn sau 120 giây
            verificationRepo.save(verification); // lưu bảng auth_email_verifications
            emailService.sendOtpEmail(request.getEmail(), otp);
            return;
        }
        // Email chưa tồn tại

        AuthUser user = new AuthUser();
        user.setFullName(request.getFullName());
        user.setStatus(UserStatus.PENDING); // chờ xác minh OTP mới chuyển ACTIVE
        System.out.println(roleRepo.findAll());
        AuthRole role = roleRepo.findByRoleName("USER").orElseThrow(() -> new RuntimeException("Role USER not found"));
        user.setRole(role);
        userRepo.save(user); // lưu bảng auth_users

        AuthAccount account = new AuthAccount();
        account.setUser(user);
        account.setProvider(AuthProviders.LOCAL);
        account.setIdentifier(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            account.setPasswordHash(passwordEncoder.encode(request.getPassword())); // băm mật khẩu, không lưu plain text
        } else {
            account.setPasswordHash(null);
        }
        accountRepo.save(account); // lưu bảng auth_accounts

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        AuthEmailVerification verification = new AuthEmailVerification();
        verification.setAuthAccount(account);
        verification.setVerificationCode(otp);
        verification.setAttemptCount(0);
        verification.setIsUsed(false);
        verification.setExpiresAt(Instant.now().plusSeconds(120));
        verificationRepo.save(verification);

        try {
            emailService.sendOtpEmail(request.getEmail(), otp); // gọi gửi email OTP
        } catch (Exception e) {
            log.error("Không thể gửi OTP tới email {}: {}", request.getEmail(), e.getMessage());

            throw new IllegalArgumentException(
                    "Địa chỉ Gmail không tồn tại hoặc không thể nhận email."
            );
        }
    }

    // Đăng nhập bằng email + mật khẩu. Đầu vào: email + mật khẩu. Trả về: JWT + thông tin cơ bản.
    // Các bước kiểm tra theo thứ tự: (1) có tài khoản LOCAL với email này không — nếu email tồn
    // tại nhưng qua provider khác (Google/GitHub) thì báo đúng provider đó, không cho đăng nhập
    // bằng mật khẩu; (2) tài khoản đã xác minh email chưa; (3) mật khẩu có khớp không;
    // (4) trạng thái tài khoản có bị PENDING/BANNED không; (5) hợp lệ thì phát JWT.
    @Override
    public AuthResponse login(LoginRequest request) {
        Optional<AuthAccount> localAccount =
                accountRepo.findByProviderAndIdentifier(
                        AuthProviders.LOCAL,
                        request.getEmail()
                );

        if (localAccount.isEmpty()) {

            Optional<AuthAccount> existingAccount =
                    accountRepo.findByIdentifier(request.getEmail());

            if (existingAccount.isPresent()) {
                throw new IllegalArgumentException(
                        AuthProviderMessageUtil.getProviderMessage(
                                existingAccount.get().getProvider()
                        )
                );
            }

            // Email hoàn toàn không tồn tại trong hệ thống
            throw new IllegalArgumentException("Không tìm thấy tài khoản.");
        }


        AuthAccount account = localAccount.get();

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
        // Sinh JWT chứa email + role, dùng cho mọi request sau này (gửi kèm header Authorization).
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

    // Xác minh OTP đăng ký. Kiểm tra: OTP chưa dùng, chưa hết hạn, đúng mã -> đánh dấu tài khoản
    // đã xác minh và chuyển user sang trạng thái ACTIVE (được phép đăng nhập).
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
        accountRepo.save(account); // lưu bảng auth_accounts: đánh dấu đã xác minh
        AuthUser user = account.getUser();
        user.setStatus(UserStatus.ACTIVE); // đổi trạng thái: PENDING -> ACTIVE, được phép đăng nhập
        userRepo.save(user);
    }

    // Gửi lại OTP mới cho email đã đăng ký (dùng khi OTP cũ hết hạn/thất lạc).
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

    // Đăng xuất — hiện chỉ log lại email, JWT vẫn còn hiệu lực tới khi hết hạn (chưa có blacklist token).
    @Override
    public void logout(String accessToken) {
        String email = jwtUtil.extractUsername(accessToken);
        System.out.println("User logout: " + email);
    }

    // Bắt đầu luồng quên mật khẩu: gửi OTP xác minh về email đã đăng ký.
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

    // Xác minh OTP quên mật khẩu, đúng thì cấp resetToken dùng một lần (hạn 10 phút) để bước sau
    // (resetPassword) dùng đặt mật khẩu mới mà không cần nhập lại OTP.
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
                Instant.now().plusSeconds(600) // resetToken hết hạn sau 10 phút
        );

        verificationRepo.save(verification);
        return new ResetTokenResponse(resetToken);
    }

    // Đặt mật khẩu mới bằng resetToken lấy từ bước xác minh OTP. Token sai/hết hạn -> báo lỗi;
    // hợp lệ thì băm mật khẩu mới, lưu lại, và vô hiệu hoá resetToken (dùng một lần).
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

        account.setPasswordHash(passwordEncoder.encode(password)); // băm mật khẩu mới
        accountRepo.save(account);

        verification.setResetToken(null);
        verification.setResetTokenExpiresAt(null);
        verification.setIsUsed(true);
        verificationRepo.save(verification);
    }
}
