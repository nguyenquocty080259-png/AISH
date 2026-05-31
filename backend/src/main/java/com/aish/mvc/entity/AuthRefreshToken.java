package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "auth_refresh_tokens")
public class AuthRefreshToken {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
@jakarta.persistence.JoinColumn(name = "auth_account_id", nullable = false)
private com.aish.mvc.entity.AuthAccount authAccount;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "token", nullable = false, length = Integer.MAX_VALUE)
private java.lang.String token;

@jakarta.persistence.Column(name = "device_info", length = Integer.MAX_VALUE)
private java.lang.String deviceInfo;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.persistence.Column(name = "ip_address", length = 100)
private java.lang.String ipAddress;

@jakarta.persistence.Column(name = "user_agent", length = Integer.MAX_VALUE)
private java.lang.String userAgent;

@jakarta.persistence.Column(name = "expires_at")
private java.time.Instant expiresAt;

@org.hibernate.annotations.ColumnDefault("false")
@jakarta.persistence.Column(name = "revoked")
private java.lang.Boolean revoked;

@jakarta.persistence.Column(name = "revoked_at")
private java.time.Instant revokedAt;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "created_at")
private java.time.Instant createdAt;



}