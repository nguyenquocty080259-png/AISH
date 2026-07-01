package com.aish.mvc.entity.doc;

import com.aish.mvc.entity.auth.AuthUser;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocDocument document;

    private Integer rating; // Số sao đánh giá

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}