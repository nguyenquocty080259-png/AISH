package com.aish.mvc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "doc_embeddings")
@EntityListeners(AuditingEntityListener.class)
public class DocEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "document_id", nullable = false)
    private DocDocument document;

    @Column(name = "chunk_index")
    private Integer chunkIndex;

    @Column(name = "chunk_text", length = Integer.MAX_VALUE)
    private String chunkText;

    @Column(name = "chunk_token_count")
    private Integer chunkTokenCount;

    @Size(max = 100)
    @Column(name = "embedding_provider", length = 100)
    private String embeddingProvider;

    @Size(max = 100)
    @Column(name = "embedding_model", length = 100)
    private String embeddingModel;

    @Size(max = 100)
    @Column(name = "embedding_status", length = 100)
    private String embeddingStatus;

    @Column(name = "embedding_reference", length = Integer.MAX_VALUE)
    private String embeddingReference;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
