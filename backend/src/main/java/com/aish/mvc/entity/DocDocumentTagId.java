package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter
@lombok.EqualsAndHashCode@jakarta.persistence.Embeddable
public class DocDocumentTagId {
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "document_id", nullable = false)
private java.lang.Long documentId;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "tag_id", nullable = false)
private java.lang.Long tagId;



}