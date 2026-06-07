package com.aish.mvc.service.auth;

import com.aish.mvc.dto.auth.AuthResponse;
import com.aish.mvc.dto.auth.LoginRequest;
import com.aish.mvc.dto.auth.SignupRequest;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthRole;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.AuthProvider;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthRoleRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.service.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository userRepo;
    private final AuthAccountRepository accountRepo;
    private final AuthRoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public void signup(SignupRequest request) {
        if(accountRepo.existsByIdentifier(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        AuthUser user = new AuthUser();
        user.setFullName(request.getFullName());

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
}