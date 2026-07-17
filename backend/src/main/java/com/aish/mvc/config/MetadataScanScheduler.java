package com.aish.mvc.config;

import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.IngestStatus;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.entity.enums.NotificationType;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.service.ai.AiModerationService;
import com.aish.mvc.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MetadataScanScheduler {
    static final int MAX_DOCUMENTS_PER_RUN = 50;
    static final long AI_CALL_DELAY_MILLIS = 2_000L;
    private static final Logger log = LoggerFactory.getLogger(MetadataScanScheduler.class);

    private final DocDocumentRepository docDocumentRepository;
    private final AiModerationService aiModerationService;
    private final NotificationService notificationService;
    private final AuthUserRepository authUserRepository;

    @Scheduled(cron = "0 0 2 * * *")
    public void scanMetadata() {
        List<DocDocument> candidates = docDocumentRepository.findMetadataScanCandidates(
                DocumentVisibility.PUBLIC, ModerationStatus.APPROVED, IngestStatus.INGESTED,
                PageRequest.of(0, MAX_DOCUMENTS_PER_RUN));
        log.info("Nightly metadata scan found {} candidate(s)", candidates.size());

        for (int index = 0; index < candidates.size(); index++) {
            processDocument(candidates.get(index));
            if (index + 1 < candidates.size()) {
                sleepBetweenCalls();
            }
        }
    }

    void processDocument(DocDocument document) {
        String previousStatus = document.getMetadataMatchStatus();
        AiModerationService.MetadataMatchResult result;
        try {
            result = aiModerationService.checkMetadata(document);
            LocalDateTime checkedAt = LocalDateTime.now();
            docDocumentRepository.stampMetadataCheck(document.getId(), result.status(), checkedAt);
            document.setMetadataMatchStatus(result.status());
            document.setMetadataCheckedAt(checkedAt);
        } catch (Exception exception) {
            log.warn("Metadata scan failed for document {}; leaving it unstamped for retry",
                    document.getId(), exception);
            return;
        }

        if (shouldNotify(previousStatus, result.status())) {
            try {
                notifyMismatch(document, result.reason());
            } catch (Exception exception) {
                log.warn("Could not send metadata mismatch notifications for document {}",
                        document.getId(), exception);
            }
        }
    }

    static boolean shouldNotify(String previousStatus, String newStatus) {
        return "LECH".equals(newStatus) && !"LECH".equals(previousStatus);
    }

    void sleepBetweenCalls() {
        try {
            Thread.sleep(AI_CALL_DELAY_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Metadata scan delay interrupted; ending scan early");
        }
    }

    private void notifyMismatch(DocDocument document, String reason) {
        String ownerMessage = "Metadata của tài liệu '" + document.getTitle()
                + "' chưa khớp nội dung. Hãy chỉnh sửa metadata và nên dùng nút \"AI gợi ý\" (FR-AI-20).";
        notificationService.createDocumentNotification(document.getUser().getId(),
                NotificationType.METADATA_MISMATCH, ownerMessage, document.getId());
        authUserRepository.findByRole_RoleNameAndStatus("ADMIN", UserStatus.ACTIVE).forEach(admin ->
                notificationService.createDocumentNotification(admin.getId(), NotificationType.METADATA_MISMATCH,
                        "Metadata tài liệu '" + document.getTitle()
                                + "' có dấu hiệu chưa khớp nội dung: " + reason, document.getId()));
    }
}
