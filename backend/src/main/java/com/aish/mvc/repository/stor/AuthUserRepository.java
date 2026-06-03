package com.aish.mvc.repository.stor;

import com.aish.mvc.entity.auth.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    // Có thể thêm hàm tìm theo username sau này
    AuthUser findByUsername(String username);
}