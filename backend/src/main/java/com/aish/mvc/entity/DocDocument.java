package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "doc_documents")
public class DocDocument {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.RESTRICT)
@jakarta.persistence.JoinColumn(name = "user_id", nullable = false)
private com.aish.mvc.entity.AuthUser user;

@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.SET_NULL)
@jakarta.persistence.JoinColumn(name = "subject_id")
private com.aish.mvc.entity.DocSubject subject;

@jakarta.validation.constraints.Size(max = 255)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "title", nullable = false)
private java.lang.String title;

@jakarta.persistence.Column(name = "description", length = Integer.MAX_VALUE)
private java.lang.String description;

@jakarta.validation.constraints.Size(max = 20)
@org.hibernate.annotations.ColumnDefault("'PRIVATE'")
@jakarta.persistence.Column(name = "visibility", length = 20)
private java.lang.String visibility;

@jakarta.validation.constraints.Size(max = 20)
@org.hibernate.annotations.ColumnDefault("'PROCESSING'")
@jakarta.persistence.Column(name = "status", length = 20)
private java.lang.String status;

@org.hibernate.annotations.ColumnDefault("0")
@jakarta.persistence.Column(name = "view_count")
private java.lang.Integer viewCount;

@org.hibernate.annotations.ColumnDefault("0")
@jakarta.persistence.Column(name = "download_count")
private java.lang.Integer downloadCount;

@org.hibernate.annotations.ColumnDefault("0.00")
@jakarta.persistence.Column(name = "average_rating", precision = 3, scale = 2)
private java.math.BigDecimal averageRating;

@jakarta.persistence.Column(name = "deleted_at")
private java.time.Instant deletedAt;

@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.SET_NULL)
@jakarta.persistence.JoinColumn(name = "deleted_by")
private com.aish.mvc.entity.AuthUser deletedBy;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "created_at")
private java.time.Instant createdAt;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "updated_at")
private java.time.Instant updatedAt;



}