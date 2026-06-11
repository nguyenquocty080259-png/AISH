package com.aish.mvc.entity.ai;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_prompts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "Learning Assistant", "Code Helper"
    @Column(nullable = false, length = 100)
    private String name;

    // "Bạn là trợ lý học tập, hãy trả lời ngắn gọn..."
    @Column(nullable = false, columnDefinition = "TEXT")
    private String systemPrompt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}