package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "auth_users")
public class AuthUser {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.Size(max = 255)
@jakarta.persistence.Column(name = "full_name")
private java.lang.String fullName;

@jakarta.persistence.Column(name = "dob")
private java.time.LocalDate dob;

@jakarta.validation.constraints.Size(max = 20)
@jakarta.persistence.Column(name = "phone_number", length = 20)
private java.lang.String phoneNumber;

@jakarta.persistence.Column(name = "avatar_url", length = Integer.MAX_VALUE)
private java.lang.String avatarUrl;

@jakarta.validation.constraints.Size(max = 20)
@org.hibernate.annotations.ColumnDefault("'ACTIVE'")
@jakarta.persistence.Column(name = "status", length = 20)
private java.lang.String status;

@jakarta.persistence.Column(name = "deleted_at")
private java.time.Instant deletedAt;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "created_at")
private java.time.Instant createdAt;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "updated_at")
private java.time.Instant updatedAt;



}