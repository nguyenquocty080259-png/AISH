package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter
@lombok.EqualsAndHashCode@jakarta.persistence.Embeddable
public class DocCollectionDocumentId {
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "collection_id", nullable = false)
private java.lang.Long collectionId;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "document_id", nullable = false)
private java.lang.Long documentId;



}