package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "auth_sms_verifications")
public class AuthSmsVerification {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
@jakarta.persistence.JoinColumn(name = "auth_account_id", nullable = false)
private com.aish.mvc.entity.AuthAccount authAccount;

@jakarta.validation.constraints.Size(max = 10)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "otp_code", nullable = false, length = 10)
private java.lang.String otpCode;

@org.hibernate.annotations.ColumnDefault("0")
@jakarta.persistence.Column(name = "attempt_count")
private java.lang.Integer attemptCount;

@org.hibernate.annotations.ColumnDefault("false")
@jakarta.persistence.Column(name = "is_used")
private java.lang.Boolean isUsed;

@jakarta.persistence.Column(name = "expires_at")
private java.time.Instant expiresAt;

@jakarta.persistence.Column(name = "verified_at")
private java.time.Instant verifiedAt;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "created_at")
private java.time.Instant createdAt;



}