package com.aish.mvc.entity.interaction;

import com.aish.mvc.entity.enums.CaseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// 1 tin nhắn trong thread trao đổi 2 chiều của 1 case REPORT/APPEAL. case_id polymorphic
// (trỏ tới reports.id hoặc moderation_appeals.id tùy case_type) -> KHÔNG FK cứng/@ManyToOne,
// vì cùng 1 cột không thể tham chiếu 2 bảng khác nhau.
@Entity
@Table(name = "case_message", indexes = @Index(columnList = "case_type, case_id, created_at"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_type", nullable = false)
    private CaseType caseType;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "sender_user_id", nullable = false)
    private Long senderUserId;

    // Snapshot role name lúc gửi (vd "USER"/"ADMIN") -> không đổi nếu sau này role của user đổi.
    @Column(name = "sender_role", nullable = false)
    private String senderRole;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
