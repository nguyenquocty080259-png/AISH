package com.aish.mvc.entity.doc;

import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.AppealStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// Kháng cáo của chủ tài liệu sau khi bị AI moderation REJECTED — đi thẳng tới hàng chờ
// Admin xem xét thủ công, KHÔNG gọi lại AI (quyết định sản phẩm rõ ràng).
@Entity
@Table(name = "moderation_appeals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModerationAppeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocDocument document;

    // Người kháng cáo — phải là chủ sở hữu tài liệu (kiểm tra ở service).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private AppealStatus status = AppealStatus.APPEAL_PENDING;

    // Ghi chú (tuỳ chọn) của Admin khi approve/reject — không bắt buộc.
    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
