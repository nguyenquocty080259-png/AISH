package com.aish.mvc.repository.auth;

import com.aish.mvc.entity.auth.AuthUserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthUserProfileRepository extends JpaRepository<AuthUserProfile, Long> {

    Optional<AuthUserProfile> findByUserId(Long userId);

    Optional<AuthUserProfile> findByUsername(String username);

    boolean existsByUsername(String username);

}
