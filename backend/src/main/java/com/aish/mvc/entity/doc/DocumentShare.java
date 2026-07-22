package com.aish.mvc.entity.doc;

import com.aish.mvc.entity.enums.SharePermission;
import com.aish.mvc.entity.enums.ShareMode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// FK-lite như các bản ghi tương tác khác (Favorite, Download...): lưu documentId/userId dạng
// Long thay vì quan hệ JPA, để trash/restore tài liệu không vướng ràng buộc cascade.
// Một dòng biểu diễn hoặc: (a) share cho 1 user cụ thể (sharedWithUserId != null, mode RESTRICTED),
// hoặc (b) share qua link (sharedWithUserId == null, shareToken != null, mode ANYONE_WITH_LINK).
@Entity
@Table(name = "document_shares")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    // null khi share qua link (ANYONE_WITH_LINK); != null khi mời 1 user cụ thể (RESTRICTED).
    @Column(name = "shared_with_user_id")
    private Long sharedWithUserId;

    @Column(name = "shared_by_user_id", nullable = false)
    private Long sharedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", length = 20, nullable = false)
    @Builder.Default
    private SharePermission permission = SharePermission.VIEWER;

    @Enumerated(EnumType.STRING)
    @Column(name = "share_mode", length = 20, nullable = false)
    @Builder.Default
    private ShareMode shareMode = ShareMode.RESTRICTED;

    // Token cho link-share; null với RESTRICTED.
    @Column(name = "share_token", length = 64)
    private String shareToken;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
