package com.aish.mvc.entity.auth;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "auth_refresh_tokens")
@EntityListeners(AuditingEntityListener.class)
public class AuthRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "auth_account_id", nullable = false)
    private AuthAccount authAccount;

    @Column(name = "token", length = Integer.MAX_VALUE)
    private String token;

    @Size(max = 100)
    @Column(name = "device_info", length = 100)
    private String deviceInfo;

    @Column(name = "ip_address", length = Integer.MAX_VALUE)
    private String ipAddress;

    @Size(max = 100)
    @Column(name = "user_agent", length = 100)
    private String userAgent;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @ColumnDefault("false")
    @Column(name = "revoked")
    private Boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
