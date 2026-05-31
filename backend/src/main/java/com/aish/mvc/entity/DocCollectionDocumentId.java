package com.aish.mvc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class DocCollectionDocumentId implements Serializable {

    @Column(name = "collection_id", nullable = false)
    private Long collectionId;

    @Column(name = "document_id", nullable = false)
    private Long documentId;
}
