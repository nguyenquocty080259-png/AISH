package com.aish.mvc.entity.auth;


import com.aish.mvc.entity.doc.DocDocument;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "auth_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    private String email;

    @Column(name = "full_name")
    private String fullName;

    private String role; // Ví dụ: ROLE_USER, ROLE_ADMIN

    // Quan hệ 1 User có nhiều Documents
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<DocDocument> documents;
}