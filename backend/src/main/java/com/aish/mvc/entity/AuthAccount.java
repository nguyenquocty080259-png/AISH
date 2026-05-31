package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "auth_accounts")
public class AuthAccount {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.RESTRICT)
@jakarta.persistence.JoinColumn(name = "user_id", nullable = false)
private com.aish.mvc.entity.AuthUser user;

@jakarta.validation.constraints.Size(max = 20)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "provider", nullable = false, length = 20)
private java.lang.String provider;

@jakarta.validation.constraints.Size(max = 255)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "identifier", nullable = false)
private java.lang.String identifier;

@jakarta.validation.constraints.Size(max = 255)
@jakarta.persistence.Column(name = "password_hash")
private java.lang.String passwordHash;

@org.hibernate.annotations.ColumnDefault("false")
@jakarta.persistence.Column(name = "is_verified")
private java.lang.Boolean isVerified;

@org.hibernate.annotations.ColumnDefault("false")
@jakarta.persistence.Column(name = "is_primary")
private java.lang.Boolean isPrimary;

@org.hibernate.annotations.ColumnDefault("0")
@jakarta.persistence.Column(name = "failed_login_attempts")
private java.lang.Integer failedLoginAttempts;

@jakarta.persistence.Column(name = "last_login_at")
private java.time.Instant lastLoginAt;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "created_at")
private java.time.Instant createdAt;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "updated_at")
private java.time.Instant updatedAt;



}