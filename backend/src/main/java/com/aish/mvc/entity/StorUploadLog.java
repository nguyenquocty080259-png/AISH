package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "stor_upload_logs")
public class StorUploadLog {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
@jakarta.persistence.JoinColumn(name = "document_file_id", nullable = false)
private com.aish.mvc.entity.DocFile documentFile;

@jakarta.validation.constraints.Size(max = 20)
@jakarta.persistence.Column(name = "log_level", length = 20)
private java.lang.String logLevel;

@jakarta.persistence.Column(name = "message", length = Integer.MAX_VALUE)
private java.lang.String message;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "created_at")
private java.time.Instant createdAt;



}