package com.aish.mvc.entity.ai;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_models")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "gemini-1.5-flash", "gemini-1.5-pro", "gpt-4o"
    @Column(nullable = false, unique = true, length = 100)
    private String modelKey;

    // "Gemini Flash", "Gemini Pro", "GPT-4o"
    @Column(nullable = false, length = 100)
    private String displayName;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(name = "input_price_per1m")
    private Double inputPricePer1m;

    @Column(name = "output_price_per1m")
    private Double outputPricePer1m;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
