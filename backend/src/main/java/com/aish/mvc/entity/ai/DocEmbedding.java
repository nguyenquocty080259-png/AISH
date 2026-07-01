package com.aish.mvc.entity.doc;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "doc_embeddings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocDocument document;

    // Thứ tự chunk trong tài liệu: 0, 1, 2, ...
    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    // Số trang gốc trong PDF (1-based) — bắt buộc để tạo citation (documentId+page+snippet)
    @Column(name = "page")
    private Integer page;

    // Nội dung đoạn văn gốc — AI dùng cái này để trả lời
    @Column(name = "chunk_text", nullable = false, columnDefinition = "TEXT")
    private String chunkText;

    // Vector embedding dạng JSON: "[0.12, -0.34, 0.56, ...]"
    // Nếu sau này dùng pgvector thì đổi sang @Column(columnDefinition = "vector(768)")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String embedding;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}