package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "doc_files")
public class DocFile {
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
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.RESTRICT)
@jakarta.persistence.JoinColumn(name = "provider_id", nullable = false)
private com.aish.mvc.entity.StorProvider provider;

@jakarta.validation.constraints.Size(max = 255)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "file_name", nullable = false)
private java.lang.String fileName;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.persistence.Column(name = "file_type", length = 100)
private java.lang.String fileType;

@jakarta.persistence.Column(name = "file_size")
private java.lang.Long fileSize;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "storage_url", nullable = false, length = Integer.MAX_VALUE)
private java.lang.String storageUrl;

@org.hibernate.annotations.ColumnDefault("1")
@jakarta.persistence.Column(name = "version_number")
private java.lang.Integer versionNumber;

@org.hibernate.annotations.ColumnDefault("true")
@jakarta.persistence.Column(name = "is_current")
private java.lang.Boolean isCurrent;

@jakarta.validation.constraints.Size(max = 20)
@org.hibernate.annotations.ColumnDefault("'PENDING'")
@jakarta.persistence.Column(name = "upload_status", length = 20)
private java.lang.String uploadStatus;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "created_at")
private java.time.Instant createdAt;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "updated_at")
private java.time.Instant updatedAt;



}