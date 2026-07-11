package com.aish.mvc.repository.auth;

import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.enums.AuthProviders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthAccountRepository extends JpaRepository<AuthAccount, Long> {

    Optional<AuthAccount> findByIdentifier(String identifier);

    @Query("""
            SELECT a
            FROM AuthAccount a
            JOIN FETCH a.user u
            JOIN FETCH u.role
            WHERE a.identifier = :identifier
            """)
    Optional<AuthAccount> findByIdentifierWithUserAndRole(@Param("identifier") String identifier);

    boolean existsByIdentifier(String identifier);

    Optional<AuthAccount> findByProviderAndIdentifier(AuthProviders provider, String identifier);

    boolean existsByProviderAndIdentifier(AuthProviders provider, String identifier);

    // Dùng bởi DbSeedRunner để tìm/dọn tài khoản seed theo domain email quy ước (@seed.aish.local).
    List<AuthAccount> findByIdentifierEndingWithIgnoreCase(String suffix);
}
