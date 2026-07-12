package com.aish.mvc.tools.seed;

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Order(2)
@ConditionalOnProperty(name = "app.seed.db.enabled", havingValue = "true")
public class DbSeedUserRunner implements CommandLineRunner {

    private static final String SEED_DOMAIN = "@seed.aish.local";
    private static final int SEED_USER_COUNT = 5;

    private final AuthRoleRepository roleRepository;
    private final AuthUserRepository userRepository;
    private final AuthAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        AuthRole userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new IllegalStateException(
                        "Role USER chưa được tạo trước khi chạy user seed."));

        for (int index = 0; index < SEED_USER_COUNT; index++) {
            String fullName = VietnameseNameBank.fullNameFor(index);
            String email = VietnameseNameBank.emailFor(index, fullName, SEED_DOMAIN);
            String password = "User" + (index + 1) + "@123";
            boolean created = false;

            if (!accountRepository.existsByIdentifier(email)) {
                AuthUser user = new AuthUser();
                user.setFullName(fullName);
                user.setRole(userRole);
                user.setStatus(UserStatus.ACTIVE);
                userRepository.save(user);

                AuthAccount account = new AuthAccount();
                account.setUser(user);
                account.setProvider(AuthProviders.LOCAL);
                account.setIdentifier(email);
                account.setPasswordHash(passwordEncoder.encode(password));
                account.setIsVerified(true);
                account.setIsPrimary(true);
                accountRepository.save(account);
                created = true;
            }

            printCredentials(created, fullName, email, password);
        }
    }

    private void printCredentials(boolean created, String fullName, String email, String password) {
        System.out.println("=================================");
        System.out.println(created ? "SEED USER ACCOUNT CREATED" : "SEED USER ACCOUNT ALREADY EXISTS");
        System.out.println("Full name: " + fullName);
        System.out.println("Email    : " + email);
        System.out.println("Password : " + password);
        System.out.println("=================================");
    }
}
