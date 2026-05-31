package com.aish.mvc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "ai_messages")
@EntityListeners(AuditingEntityListener.class)
public class AiMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "conversation_id", nullable = false)
    private AiConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "related_document_id")
    private DocDocument relatedDocument;

    @Size(max = 20)
    @Column(name = "role", length = 20)
    private String role;

    @Column(name = "message", length = Integer.MAX_VALUE)
    private String message;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
