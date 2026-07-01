package com.aish.mvc.entity.doc;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// Bảng nối là entity riêng (KHÔNG @ManyToMany thuần).
// UNIQUE(collection_id, document_id): 1 tài liệu không lặp 2 lần trong cùng 1 collection.
@Entity
@Table(
        name = "collection_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_collection_item_doc", columnNames = {"collection_id", "document_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    // CHỈ lưu id của document. KHÔNG @ManyToOne DocDocument để tránh FK cứng khi doc bị xóa/ẩn.
    // TUYỆT ĐỐI KHÔNG cache title/nội dung/owner của document ở đây.
    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @CreationTimestamp
    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;
}
