package com.aish.mvc.entity.doc;

import com.aish.mvc.entity.enums.ModerationKeywordType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(
        name = "moderation_keywords",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_moderation_keywords_keyword_type",
                columnNames = {"keyword", "type"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModerationKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String keyword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ModerationKeywordType type;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    void normalizeKeyword() {
        if (keyword != null) {
            keyword = keyword.trim().toLowerCase(Locale.ROOT);
        }
    }
}
