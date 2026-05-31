package com.aish.mvc.entity;

@lombok.Getter
@lombok.Setter@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "doc_collections")
public class DocCollection {
@jakarta.persistence.Id
@jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
@jakarta.persistence.Column(name = "id", nullable = false)
private java.lang.Long id;

@jakarta.validation.constraints.NotNull
@jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
@org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.RESTRICT)
@jakarta.persistence.JoinColumn(name = "owner_id", nullable = false)
private com.aish.mvc.entity.AuthUser owner;

@jakarta.validation.constraints.Size(max = 255)
@jakarta.validation.constraints.NotNull
@jakarta.persistence.Column(name = "name", nullable = false)
private java.lang.String name;

@jakarta.persistence.Column(name = "description", length = Integer.MAX_VALUE)
private java.lang.String description;

@jakarta.validation.constraints.Size(max = 20)
@jakarta.persistence.Column(name = "color", length = 20)
private java.lang.String color;

@jakarta.validation.constraints.Size(max = 100)
@jakarta.persistence.Column(name = "icon", length = 100)
private java.lang.String icon;

@jakarta.validation.constraints.Size(max = 20)
@org.hibernate.annotations.ColumnDefault("'PRIVATE'")
@jakarta.persistence.Column(name = "visibility", length = 20)
private java.lang.String visibility;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "created_at")
private java.time.Instant createdAt;

@org.hibernate.annotations.ColumnDefault("CURRENT_TIMESTAMP")
@jakarta.persistence.Column(name = "updated_at")
private java.time.Instant updatedAt;



}