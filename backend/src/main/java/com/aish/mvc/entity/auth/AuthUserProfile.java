package com.aish.mvc.entity.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "auth_user_profiles")
public class AuthUserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private AuthUser user;

    // Public Profile
    @Column(unique = true, length = 50)
    private String username;

    @Column(columnDefinition = "TEXT")
    private String bio;

    // Personal
    private LocalDate dob;

    @Column(length = 20)
    private String gender;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // Academic
    @Column(length = 255)
    private String university;

    @Column(length = 255)
    private String faculty;

    @Column(length = 255)
    private String major;

    // Location
    private String country;

    private String city;

    // Social
    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
