package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "doc_embeddings")
public class DocEmbedding {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
@jakarta.persistence.JoinColumn(name = "document_id", nullable = false)
private com.aish.mvc.entity.DocDocument document;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "chunk_index", nullable = false)
private java.lang.Integer chunkIndex;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "chunk_text", nullable = false, length = Integer.MAX_VALUE)
private java.lang.String chunkText;

@jakarta.persistence.Column(name = "chunk_token_count")
private java.lang.Integer chunkTokenCount;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.persistence.Column(name = "embedding_provider", length = 100)
private java.lang.String embeddingProvider;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.persistence.Column(name = "embedding_model", length = 100)
private java.lang.String embeddingModel;

@jakarta.validation.constraints.Size(max = 20)
@jakarta.persistence.Column(name = "embedding_status", length = 20)
private java.lang.String embeddingStatus;

@jakarta.persistence.Column(name = "embedding_reference", length = Integer.MAX_VALUE)
private java.lang.String embeddingReference;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "created_at")
private java.time.Instant createdAt;



}