package com.aish.mvc.repository.auth;

import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthEmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthEmailVerificationRepository
        extends JpaRepository<AuthEmailVerification, Long> {

    Optional<AuthEmailVerification>
    findTopByAuthAccountOrderByCreatedAtDesc(
            AuthAccount authAccount
    );
}