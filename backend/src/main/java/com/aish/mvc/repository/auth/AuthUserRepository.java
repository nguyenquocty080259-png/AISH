package com.aish.mvc.repository.auth;

import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    long countByRole_RoleNameAndStatus(String roleName, UserStatus status);

    List<AuthUser> findByRole_RoleNameAndStatus(String roleName, UserStatus status);

    @Query("""
    SELECT u
    FROM AuthUser u
    LEFT JOIN FETCH u.role LEFT JOIN FETCH u.profile ORDER BY u.id""")
    List<AuthUser> findAllForAdmin();

    @Query("""
    SELECT a
    FROM AuthAccount a
    JOIN FETCH a.user
    JOIN FETCH a.user.role""")
    List<AuthAccount> findAllAccountsForAdmin();
}
