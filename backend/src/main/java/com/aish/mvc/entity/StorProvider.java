package com.aish.mvc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "stor_providers")
@EntityListeners(AuditingEntityListener.class)
public class StorProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "provider_name", nullable = false, length = 100)
    private String providerName;

    @Column(name = "base_url", length = Integer.MAX_VALUE)
    private String baseUrl;

    @Size(max = 100)
    @Column(name = "provider_type", length = 100)
    private String providerType;

    @Column(name = "api_endpoint", length = Integer.MAX_VALUE)
    private String apiEndpoint;

    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
