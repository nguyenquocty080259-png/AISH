package com.aish.mvc.entity;

import com.aish.mvc.entity.enums.UploadStatus;
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
@Table(name = "doc_files")
public class DocFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "document_id", nullable = false)
    private DocDocument document;

    @Size(max = 255)
    @NotNull
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Size(max = 100)
    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @NotNull
    @Column(name = "storage_url", nullable = false, length = Integer.MAX_VALUE)
    private String storageUrl;

    @ColumnDefault("1")
    @Column(name = "version_number")
    private Integer versionNumber = 1;

    @ColumnDefault("true")
    @Column(name = "is_current")
    private Boolean isCurrent = true;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PENDING'")
    @Column(name = "upload_status", length = 20)
    private UploadStatus uploadStatus = UploadStatus.PENDING;
}
