package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "doc_previews")
public class DocPreview {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.OneToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
@jakarta.persistence.JoinColumn(name = "document_id", nullable = false)
private com.aish.mvc.entity.DocDocument document;

@jakarta.persistence.Column(name = "thumbnail_url", length = Integer.MAX_VALUE)
private java.lang.String thumbnailUrl;

@jakarta.persistence.Column(name = "preview_url", length = Integer.MAX_VALUE)
private java.lang.String previewUrl;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "created_at")
private java.time.Instant createdAt;



}