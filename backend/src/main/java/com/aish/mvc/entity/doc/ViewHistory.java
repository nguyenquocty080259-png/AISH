package com.aish.mvc.entity.doc;

import com.aish.mvc.entity.auth.AuthUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// Lịch sử xem tài liệu ("Tiếp tục học"). Mỗi cặp (user, document) chỉ 1 dòng.
// KHÔNG lưu title/nội dung/snapshot — chỉ 3 field ý nghĩa: user, documentId, viewedAt.
@Entity
@Table(
        name = "view_histories",
        uniqueConstraints = @UniqueConstraint(name = "uk_view_history_user_doc", columnNames = {"user_id", "document_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    // CHỈ lưu id document. KHÔNG @ManyToOne DocDocument (tránh FK cứng khi doc bị xóa/ẩn).
    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;
}
