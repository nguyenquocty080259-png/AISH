package com.aish.mvc.config;

import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthRole;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.AuthProviders;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthRoleRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DbSeedRunner implements CommandLineRunner {

    private final AuthRoleRepository roleRepository;
    private final AuthUserRepository userRepository;
    private final AuthAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ===== ROLE ADMIN =====
        AuthRole adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> {
                    AuthRole role = new AuthRole();
                    role.setRoleName("ADMIN");
                    return roleRepository.save(role);
                });

        // ===== ROLE USER =====
        roleRepository.findByRoleName("USER")
                .orElseGet(() -> {
                    AuthRole role = new AuthRole();
                    role.setRoleName("USER");
                    return roleRepository.save(role);
                });

        // ===== ADMIN ACCOUNT =====
        if (!accountRepository.existsByIdentifier("admin@aish.com")) {

            AuthUser admin = new AuthUser();
            admin.setFullName("System Administrator");
            admin.setStatus(UserStatus.ACTIVE);
            admin.setRole(adminRole);

            userRepository.save(admin);

            AuthAccount account = new AuthAccount();
            account.setUser(admin);
            account.setProvider(AuthProviders.LOCAL);
            account.setIdentifier("admin@aish.com");
            account.setPasswordHash(passwordEncoder.encode("Admin@123"));
            account.setIsVerified(true);
            account.setIsPrimary(true);

            accountRepository.save(account);

            System.out.println("=================================");
            System.out.println("ADMIN ACCOUNT CREATED");
            System.out.println("Email    : admin@aish.com");
            System.out.println("Password : Admin@123");
            System.out.println("=================================");
        }
    }
}