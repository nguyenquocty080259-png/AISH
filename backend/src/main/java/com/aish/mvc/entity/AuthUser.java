package com.aish.mvc.entity;

import com.aish.mvc.entity.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "auth_users")
public class AuthUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "dob")
    private LocalDate dob;

    @Size(max = 20)
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "avatar_url", length = Integer.MAX_VALUE)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
