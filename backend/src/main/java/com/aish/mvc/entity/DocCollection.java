package com.aish.mvc.entity;

import com.aish.mvc.entity.enums.Visibility;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "doc_collections")
public class DocCollection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "owner_id", nullable = false)
    private AuthUser owner;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Size(max = 20)
    @Column(name = "color", length = 20)
    private String color;

    @Size(max = 100)
    @Column(name = "icon", length = 100)
    private String icon;

    @Size(max = 255)
    @Column(name = "title")
    private String title;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PRIVATE'")
    @Column(name = "visibility", length = 20)
    private Visibility visibility = Visibility.PRIVATE;
}
