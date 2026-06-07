package com.aish.mvc.entity.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "auth_roles")
@Getter
@Setter
@NoArgsConstructor
public class AuthRole {

    @Id
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;

    @ManyToMany(mappedBy = "authRoles")
    private Set<AuthUser> users = new HashSet<>();
}
