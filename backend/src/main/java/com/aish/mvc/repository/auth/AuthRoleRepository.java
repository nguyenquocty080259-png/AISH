package com.aish.mvc.repository.auth;

import com.aish.mvc.entity.auth.AuthRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRoleRepository extends JpaRepository<AuthRole, Long> {
    Optional<AuthRole> findByRoleName(String roleName);
}
