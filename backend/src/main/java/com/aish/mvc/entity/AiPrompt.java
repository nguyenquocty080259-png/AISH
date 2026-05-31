package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "ai_prompts")
public class AiPrompt {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.Size(max = 255)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "prompt_name", nullable = false)
private java.lang.String promptName;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "system_prompt", nullable = false, length = Integer.MAX_VALUE)
private java.lang.String systemPrompt;

@org.hibernate.annotations.ColumnDefault("1")
@jakarta.persistence.Column(name = "version")
private java.lang.Integer version;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "created_at")
private java.time.Instant createdAt;



}