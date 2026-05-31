package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "doc_document_tags")
public class DocDocumentTag {
@jakarta.persistence.EmbeddedId
private com.aish.mvc.entity.DocDocumentTagId id;

@jakarta.persistence.MapsId("documentId")
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
@jakarta.persistence.JoinColumn(name = "document_id", nullable = false)
private com.aish.mvc.entity.DocDocument document;

@jakarta.persistence.MapsId("tagId")
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
@jakarta.persistence.JoinColumn(name = "tag_id", nullable = false)
private com.aish.mvc.entity.DocTag tag;



}