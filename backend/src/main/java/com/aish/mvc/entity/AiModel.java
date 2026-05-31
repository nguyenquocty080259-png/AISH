package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "ai_models")
public class AiModel {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "model_name", nullable = false, length = 100)
private java.lang.String modelName;

@jakarta.validation.constraints.Size(max = 50)
@jakarta.persistence.Column(name = "provider", length = 50)
private java.lang.String provider;

@jakarta.validation.constraints.Size(max = 50)
@jakarta.persistence.Column(name = "model_version", length = 50)
private java.lang.String modelVersion;

@jakarta.validation.constraints.Size(max = 20)
@jakarta.persistence.Column(name = "model_type", length = 20)
private java.lang.String modelType;

@jakarta.persistence.Column(name = "max_context_tokens")
private java.lang.Integer maxContextTokens;

@org.hibernate.annotations.ColumnDefault("true")
@jakarta.persistence.Column(name = "is_active")
private java.lang.Boolean isActive;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "created_at")
private java.time.Instant createdAt;



}