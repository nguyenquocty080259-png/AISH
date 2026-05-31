package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "doc_collection_documents")
public class DocCollectionDocument {
@jakarta.persistence.EmbeddedId
private com.aish.mvc.entity.DocCollectionDocumentId id;

@jakarta.persistence.MapsId("collectionId")
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
@jakarta.persistence.JoinColumn(name = "collection_id", nullable = false)
private com.aish.mvc.entity.DocCollection collection;

@jakarta.persistence.MapsId("documentId")
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
@jakarta.persistence.JoinColumn(name = "document_id", nullable = false)
private com.aish.mvc.entity.DocDocument document;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.RESTRICT)
@jakarta.persistence.JoinColumn(name = "added_by", nullable = false)
private com.aish.mvc.entity.AuthUser addedBy;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "added_at")
private java.time.Instant addedAt;

@jakarta.persistence.Column(name = "note", length = Integer.MAX_VALUE)
private java.lang.String note;



}