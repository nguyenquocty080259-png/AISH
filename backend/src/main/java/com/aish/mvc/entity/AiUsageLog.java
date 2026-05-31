package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "ai_usage_logs")
public class AiUsageLog {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
@jakarta.persistence.JoinColumn(name = "conversation_id", nullable = false)
private com.aish.mvc.entity.AiConversation conversation;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.RESTRICT)
@jakarta.persistence.JoinColumn(name = "ai_model_id", nullable = false)
private com.aish.mvc.entity.AiModel aiModel;

@jakarta.validation.constraints.Size(max = 20)
@jakarta.persistence.Column(name = "provider_response_status", length = 20)
private java.lang.String providerResponseStatus;

@jakarta.persistence.Column(name = "prompt_tokens")
private java.lang.Integer promptTokens;

@jakarta.persistence.Column(name = "response_tokens")
private java.lang.Integer responseTokens;

@jakarta.persistence.Column(name = "total_tokens")
private java.lang.Integer totalTokens;

@jakarta.persistence.Column(name = "estimated_cost", precision = 15, scale = 6)
private java.math.BigDecimal estimatedCost;

@jakarta.persistence.Column(name = "response_time_ms")
private java.lang.Integer responseTimeMs;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "created_at")
private java.time.Instant createdAt;



}