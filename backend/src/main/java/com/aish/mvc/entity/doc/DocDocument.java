package com.aish.mvc.entity.doc;

import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.enums.DocumentStatus;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.IngestStatus;
import com.aish.mvc.entity.enums.ModerationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PROCESSING;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 20)
    @Builder.Default
    private DocumentVisibility visibility = DocumentVisibility.PRIVATE;

    // DEC-035: kết quả kiểm duyệt AI cho lần chuyển PUBLIC gần nhất — dùng cho Admin flagged queue.
    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", length = 20)
    @Builder.Default
    private ModerationStatus moderationStatus = ModerationStatus.NOT_REQUIRED;

    @Column(name = "moderation_reason", columnDefinition = "TEXT")
    private String moderationReason;

    // Trạng thái AI-ingest — set khi ingest() chạy xong (INGESTED) hoặc phát hiện định
    // dạng không hỗ trợ (UNSUPPORTED_FORMAT), để FE không cần gọi lại ingest để biết.
    @Enumerated(EnumType.STRING)
    @Column(name = "ingest_status", length = 20)
    @Builder.Default
    private IngestStatus ingestStatus = IngestStatus.NOT_INGESTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocFile> files = new ArrayList<>();

    // 1 tài liệu có nhiều môn học, 1 môn học có nhiều tài liệu (nhiều-nhiều)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "document_subjects",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    @Builder.Default
    private java.util.Set<Subject> subjects = new java.util.HashSet<>();

    // 1 tài liệu có nhiều tag, 1 tag gắn nhiều tài liệu (nhiều-nhiều). Bảng nối document_tags đã tồn tại.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "document_tags",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private java.util.Set<Tag> tags = new java.util.HashSet<>();

    public void addFile(DocFile file) {
        if (this.files == null) this.files = new ArrayList<>();
        this.files.add(file);
        file.setDocument(this);
    }
}