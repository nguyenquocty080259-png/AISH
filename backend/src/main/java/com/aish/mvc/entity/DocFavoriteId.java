package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter
@lombok.EqualsAndHashCode@jakarta.persistence.Embeddable
public class DocFavoriteId {
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "user_id", nullable = false)
private java.lang.Long userId;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "document_id", nullable = false)
private java.lang.Long documentId;



}