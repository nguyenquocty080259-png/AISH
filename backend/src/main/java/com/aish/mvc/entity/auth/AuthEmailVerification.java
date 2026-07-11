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
@Table(name = "auth_email_verifications")
@EntityListeners(AuditingEntityListener.class)
public class AuthEmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "auth_account_id", nullable = false)
    private AuthAccount authAccount;

    @Size(max = 10)
    @Column(name = "verification_code", length = 10)
    private String verificationCode;

    @Column(name = "attempt_count")
    private Integer attemptCount;

    @ColumnDefault("false")
    @Column(name = "is_used")
    private Boolean isUsed = false;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expires_at")
    private Instant resetTokenExpiresAt;
}
