package com.aish.mvc.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "doc_files")
@Data
public class DocumentFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "provider_id")
    private Long providerId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "storage_url")
    private String storageUrl;

    @Column(name = "is_current")
    private Boolean isCurrent = true;
}