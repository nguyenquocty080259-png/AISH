package com.aish.mvc.entity.auth;


import com.aish.mvc.entity.enums.AuthProviders;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "auth_accounts")
public class AuthAccount  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "provider", nullable = false, length = 20)
    private AuthProviders provider;

    @Size(max = 255)
    @Column(name = "identifier")
    private String identifier;

    @Column(name = "password_hash", length = Integer.MAX_VALUE)
    private String passwordHash;

    @ColumnDefault("false")
    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @ColumnDefault("true")
    @Column(name = "is_primary")
    private Boolean isPrimary = true;

    @ColumnDefault("0")
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_login_at")
    private java.time.Instant lastLoginAt;
}
