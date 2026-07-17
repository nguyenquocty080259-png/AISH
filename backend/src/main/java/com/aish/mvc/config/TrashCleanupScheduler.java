package com.aish.mvc.config;

import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUserProfile;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserProfileRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.service.doc.DocumentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TrashCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(TrashCleanupScheduler.class);
    private static final int DEFAULT_RETENTION_DAYS = 30;

    private final DocDocumentRepository docDocumentRepository;
    private final AuthUserProfileRepository authUserProfileRepository;
    private final AuthAccountRepository authAccountRepository;
    private final DocumentService documentService;

    // Chạy mỗi ngày lúc 3h sáng: xóa cứng tài liệu đã ở thùng rác quá số ngày lưu trữ riêng
    // của CHỦ tài liệu (AuthUserProfile.trashRetentionDays, mặc định 30 ngày nếu chưa cấu hình).
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldTrash() {
        LocalDateTime now = LocalDateTime.now();
        List<DocDocument> trashed = docDocumentRepository.findByDeletedAtIsNotNull();

        SecurityContext previousContext = SecurityContextHolder.getContext();
        try {
            for (DocDocument doc : trashed) {
                try {
                    int retentionDays = retentionDaysForOwner(doc);
                    if (doc.getDeletedAt().isBefore(now.minusDays(retentionDays))) {
                        runAsOwner(doc);
                        documentService.permanentDeleteDocument(doc.getId());
                    }
                } catch (Exception exception) {
                    log.warn("Không thể dọn tài liệu id={} khỏi thùng rác: {}",
                            doc.getId(), exception.getMessage());
                }
            }
        } finally {
            SecurityContextHolder.setContext(previousContext);
        }
    }

    private int retentionDaysForOwner(DocDocument doc) {
        Long ownerId = doc.getUser() != null ? doc.getUser().getId() : null;
        if (ownerId == null) return DEFAULT_RETENTION_DAYS;
        return authUserProfileRepository.findByUserId(ownerId)
                .map(AuthUserProfile::getTrashRetentionDays)
                .orElse(DEFAULT_RETENTION_DAYS);
    }

    // permanentDeleteDocument() tự kiểm tra quyền sở hữu qua SecurityContext hiện tại (giống luồng
    // user tự xoá), nên job nền chạy dưới danh nghĩa hệ thống phải "đóng vai" đúng chủ tài liệu
    // trước khi gọi, nếu không sẽ luôn bị ForbiddenException.
    private void runAsOwner(DocDocument doc) {
        Long ownerId = doc.getUser().getId();
        AuthAccount account = authAccountRepository.findFirstByUser_IdAndIsPrimaryTrue(ownerId)
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy tài khoản của chủ tài liệu id=" + ownerId));
        SecurityContext workerContext = SecurityContextHolder.createEmptyContext();
        workerContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                account.getIdentifier(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        SecurityContextHolder.setContext(workerContext);
    }
}
