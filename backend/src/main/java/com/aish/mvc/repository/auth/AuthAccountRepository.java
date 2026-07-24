package com.aish.mvc.repository.auth;

import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.AuthProviders;
import com.aish.mvc.entity.enums.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthAccountRepository extends JpaRepository<AuthAccount, Long> {

    Optional<AuthAccount> findByIdentifier(String identifier);

    // READ-ONLY: resolve 1 email -> account bất kể hoa/thường (email local KHÔNG được normalize
    // lúc đăng ký, nên cần so khớp không phân biệt hoa/thường). Trả List để không vỡ nếu vì lệch
    // hoa/thường mà tồn tại nhiều bản ghi; tầng service tự chọn tài khoản phù hợp.
    List<AuthAccount> findByIdentifierIgnoreCase(String identifier);

    @Query("""
            SELECT a
            FROM AuthAccount a
            JOIN FETCH a.user u
            JOIN FETCH u.role
            WHERE a.identifier = :identifier
            """)
    Optional<AuthAccount> findByIdentifierWithUserAndRole(@Param("identifier") String identifier);

    boolean existsByIdentifier(String identifier);
    @Query("""
        select a
        from AuthAccount a
        join fetch a.user u
        join fetch u.role
        where a.provider = :provider
        and a.identifier = :identifier
        """)
    Optional<AuthAccount> findByProviderAndIdentifier(@Param("provider") AuthProviders provider, @Param("identifier") String identifier);

    Optional<AuthAccount> findByUserAndProvider(AuthUser user, AuthProviders provider);

    boolean existsByProviderAndIdentifier(AuthProviders provider, String identifier);

    // Dùng bởi DbSeedRunner để tìm/dọn tài khoản seed theo domain email quy ước (@seed.aish.local).
    List<AuthAccount> findByIdentifierEndingWithIgnoreCase(String suffix);

    @Query("SELECT a FROM AuthAccount a JOIN FETCH a.user u JOIN FETCH u.role r " +
            "WHERE a.isPrimary = true " +
            "AND (:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) " +
            "OR LOWER(a.identifier) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) " +
            "AND (:role IS NULL OR LOWER(r.roleName) = LOWER(CAST(:role AS string))) " +
            "AND (:status IS NULL OR u.status = :status) ORDER BY u.id ASC")
    List<AuthAccount> searchUsersForAdminTool(@Param("keyword") String keyword,
                                               @Param("role") String role,
                                               @Param("status") UserStatus status,
                                               Pageable pageable);

    Optional<AuthAccount> findFirstByUser_IdAndIsPrimaryTrue(Long userId);
}
