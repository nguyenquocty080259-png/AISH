package com.aish.mvc.dto.auth.admin;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminUserResponseDTO {
    private Long id;

    private String fullName;

    private String email;

    private String avatarUrl;

    private String role;

    private String status;

    private Boolean online;

    private Instant lastLoginAt;

    private Instant deletedAt;
}
