package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "stor_providers")
public class StorProvider {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "provider_name", nullable = false, length = 100)
private java.lang.String providerName;

@jakarta.validation.constraints.Size(max = 50)
@jakarta.persistence.Column(name = "provider_type", length = 50)
private java.lang.String providerType;

@jakarta.persistence.Column(name = "api_endpoint", length = Integer.MAX_VALUE)
private java.lang.String apiEndpoint;

@org.hibernate.annotations.ColumnDefault("true")
@jakarta.persistence.Column(name = "is_active")
private java.lang.Boolean isActive;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "created_at")
private java.time.Instant createdAt;



}