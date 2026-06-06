package com.aish.mvc.service.auth;

import com.aish.mvc.dto.auth.AuthResponse;
import com.aish.mvc.dto.auth.LoginRequest;
import com.aish.mvc.dto.auth.SignupRequest;
import com.aish.mvc.dto.auth.VerifyOtpRequest;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthEmailVerification;
import com.aish.mvc.entity.auth.AuthRole;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.AuthProvider;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthEmailVerificationRepository;
import com.aish.mvc.repository.auth.AuthRoleRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.service.auth.security.JwtUtil;
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

    @Override
    public void signup(SignupRequest request) {
        if(accountRepo.existsByIdentifier(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        AuthUser user = new AuthUser();
        user.setFullName(request.getFullName());
        user.setStatus(UserStatus.PENDING);

        AuthRole role =
                roleRepo.findByRoleName("STUDENT")
                        .orElseThrow(
                                () -> new RuntimeException("Role STUDENT not found")
                        );

        user.getAuthRoles().add(role);

        userRepo.save(user);

        AuthAccount account = new AuthAccount();
        account.setUser(user);

        account.setProvider(AuthProvider.LOCAL);

        account.setIdentifier(request.getEmail());

        account.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        accountRepo.save(account);
        String otp =
                String.valueOf(
                        (int)(
                                Math.random()
                                        * 900000
                        ) + 100000
                );
        AuthEmailVerification verification =
                new AuthEmailVerification();

        verification.setAuthAccount(account);

        verification.setVerificationCode(otp);

        verification.setAttemptCount(0);

        verification.setIsUsed(false);

        verification.setExpiresAt(
                Instant.now()
                        .plusSeconds(300)
        );

        emailVerificationRepo.save(
                verification
        );
        System.out.println(
                "OTP = " + otp
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        AuthAccount account =
                accountRepo.findByIdentifier(
                                request.getEmail()
                        )
                        .orElseThrow(
                                () -> new RuntimeException("User not found")
                        );
        if (!Boolean.TRUE.equals(account.getIsVerified())) {
            throw new RuntimeException(
                    "Please verify your email first"
            );
        }
        if(!passwordEncoder.matches(
                request.getPassword(),
                account.getPasswordHash()
        )){
            throw new RuntimeException(
                    "Invalid password"
            );
        }

        String accessToken =
                jwtUtil.generateToken(
                        request.getEmail()
                );

        String refreshToken =
                UUID.randomUUID().toString();

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer"
        );
    }

    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        AuthAccount account =
                accountRepo
                        .findByIdentifier(
                                request.getEmail()
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Email not found"
                                        )
                        );

        AuthEmailVerification verification =
                emailVerificationRepo
                        .findTopByAuthAccountOrderByCreatedAtDesc(
                                account
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "OTP not found"
                                        )
                        );

        if(Boolean.TRUE.equals(
                verification.getIsUsed()
        )){
            throw new RuntimeException(
                    "OTP already used"
            );
        }

        if(
                Instant.now()
                        .isAfter(
                                verification.getExpiresAt()
                        )
        ){
            throw new RuntimeException(
                    "OTP expired"
            );
        }

        if(
                !verification
                        .getVerificationCode()
                        .equals(
                                request.getOtp()
                        )
        ){
            throw new RuntimeException(
                    "Invalid OTP"
            );
        }

        verification.setIsUsed(true);

        verification.setVerifiedAt(
                Instant.now()
        );

        emailVerificationRepo.save(
                verification
        );

        account.setIsVerified(true);

        accountRepo.save(account);

        AuthUser user =
                account.getUser();

        user.setStatus(
                UserStatus.ACTIVE
        );

        userRepo.save(user);
    }
}