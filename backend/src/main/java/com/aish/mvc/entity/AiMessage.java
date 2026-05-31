package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "ai_messages")
public class AiMessage {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
@jakarta.persistence.JoinColumn(name = "conversation_id", nullable = false)
private com.aish.mvc.entity.AiConversation conversation;

@jakarta.validation.constraints.Size(max = 20)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "role", nullable = false, length = 20)
private java.lang.String role;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "message", nullable = false, length = Integer.MAX_VALUE)
private java.lang.String message;

@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.SET_NULL)
@jakarta.persistence.JoinColumn(name = "related_document_id")
private com.aish.mvc.entity.DocDocument relatedDocument;

@jakarta.persistence.Column(name = "token_count")
private java.lang.Integer tokenCount;

@jakarta.persistence.Column(name = "response_time_ms")
private java.lang.Integer responseTimeMs;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "created_at")
private java.time.Instant createdAt;



}