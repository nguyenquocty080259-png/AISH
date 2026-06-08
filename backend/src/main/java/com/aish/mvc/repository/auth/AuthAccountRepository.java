package com.aish.mvc.repository.auth;

import com.aish.mvc.entity.auth.AuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthAccountRepository extends JpaRepository<AuthAccount, Long> {
    Optional<AuthAccount> findByIdentifier(String identifier);

    boolean existsByIdentifier(String identifier);
}
