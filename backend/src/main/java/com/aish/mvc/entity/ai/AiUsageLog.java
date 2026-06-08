package com.aish.mvc.entity.ai;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_usage_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tin nhắn nào sinh ra log này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private AiMessage message;

    // Dùng model nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private AiModel model;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    // Tổng token = input + output, lưu sẵn để query nhanh
    @Column(name = "total_tokens")
    private Integer totalTokens;

    // Chi phí USD, tính từ token * giá model
    @Column(name = "cost_usd", precision = 10)
    private Double costUsd;

    // Thời gian Gemini phản hồi (ms)
    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}