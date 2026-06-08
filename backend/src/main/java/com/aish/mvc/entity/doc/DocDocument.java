package com.aish.mvc.entity.doc;

import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.DocumentStatus;
import com.aish.mvc.entity.enums.DocumentVisibility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "doc_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_status", length = 20)
    private DocumentStatus status = DocumentStatus.PROCESSING;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 20)
    private DocumentVisibility visibility = DocumentVisibility.PRIVATE;

    // Quan hệ với User (Chủ sở hữu)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // THÊM ĐOÀN NÀY: Lưu thời gian xóa mềm, nếu bằng null nghĩa là file chưa bị xóa
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Quan hệ với danh sách các file vật lý đính kèm
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocFile> files;
}