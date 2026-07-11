package com.aish.mvc.entity.doc;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "doc_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName; // Tên file gốc (VD: bài_tập.pdf)

    @Column(nullable = false)
    private String fileUrl;  // Đường dẫn lưu trữ (local: tên file | cloud: URL Cloudinary)

    private String fileType; // Loại file (VD: application/pdf)

    private Long fileSize;   // Dung lượng file (bytes)

    private String publicId;     // public_id trên Cloudinary

    private String resourceType; // image | raw | local

    // Quan hệ: Nhiều file thuộc về 1 Document
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocDocument document;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;
    // ===== THÊM MỚI =====
    // Đường dẫn thumbnail TƯƠNG ĐỐI trong thư mục uploads (vd. "thumbnails/xxx.png").
    // Null nếu định dạng không render được (docx, txt...) — FE fallback về icon.
    private String thumbnailUrl;
}