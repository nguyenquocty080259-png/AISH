package com.aish.mvc.entity.doc;

import com.aish.mvc.entity.auth.AuthUser;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(FavoriteId.class) // composite primary key theo ERD của bạn
public class Favorite {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}

// Class định nghĩa khóa chính phức hợp
@Data @NoArgsConstructor @AllArgsConstructor
class FavoriteId implements java.io.Serializable {
    private Long userId;
    private Long documentId;
}