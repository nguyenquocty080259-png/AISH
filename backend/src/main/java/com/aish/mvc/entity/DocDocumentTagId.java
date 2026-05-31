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
public class DocDocumentTagId implements Serializable {

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;
}
