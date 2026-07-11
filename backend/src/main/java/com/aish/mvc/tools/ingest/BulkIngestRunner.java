package com.aish.mvc.tools.ingest;

import com.aish.mvc.dto.ai.IngestResponseDTO;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.IngestStatus;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.service.doc.DocEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Polling worker cho tài liệu NOT_INGESTED. Mỗi cycle xử lý tuần tự một batch nhỏ và nghỉ giữa
 * hai tài liệu để bảo vệ quota Gemini. Bean chỉ tồn tại khi app.ingest.auto.enabled=true.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.ingest.auto.enabled", havingValue = "true")
public class BulkIngestRunner {

    private static final Logger log = LoggerFactory.getLogger(BulkIngestRunner.class);
    private static final String SYSTEM_ADMIN_EMAIL = "admin@aish.com";
    private static final int BATCH_SIZE = 5;
    private static final int QUERY_PAGE_SIZE = 50;
    private static final int MAX_FAILURES = 3;
    private static final long BETWEEN_DOCUMENT_DELAY_MS = 4_000L;

    private final DocDocumentRepository docDocumentRepository;
    private final DocEmbeddingService docEmbeddingService;
    private final AuthAccountRepository authAccountRepository;

    // Chỉ sống trong process hiện tại: restart app cho phép thử lại các document đã chạm ngưỡng.
    private final Map<Long, Integer> failureCounts = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 60_000L, initialDelay = 30_000L)
    public void pollNotIngestedDocuments() {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        AuthAccount adminAccount = authAccountRepository.findByIdentifier(SYSTEM_ADMIN_EMAIL)
                .orElse(null);
        if (adminAccount == null || adminAccount.getUser() == null
                || adminAccount.getUser().getRole() == null
                || !"ADMIN".equals(adminAccount.getUser().getRole().getRoleName())) {
            log.error("Auto-ingest bỏ qua cycle: không tìm thấy SYSTEM admin hợp lệ {}.", SYSTEM_ADMIN_EMAIL);
            return;
        }

        List<DocDocument> targets = selectTargets();
        if (targets.isEmpty()) {
            return;
        }

        SecurityContext previousContext = SecurityContextHolder.getContext();
        SecurityContext workerContext = SecurityContextHolder.createEmptyContext();
        workerContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                SYSTEM_ADMIN_EMAIL, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        SecurityContextHolder.setContext(workerContext);

        try {
            log.info("Auto-ingest bắt đầu cycle tuần tự với {} document.", targets.size());
            for (int index = 0; index < targets.size(); index++) {
                if (Thread.currentThread().isInterrupted()) {
                    log.info("Auto-ingest dừng cycle do ứng dụng đang shutdown.");
                    break;
                }

                DocDocument document = targets.get(index);
                processOne(document.getId());

                if (index < targets.size() - 1 && !sleepBetweenDocuments()) {
                    break;
                }
            }
        }
        finally {
            SecurityContextHolder.setContext(previousContext);
        }
    }

    private List<DocDocument> selectTargets() {
        List<DocDocument> selected = new ArrayList<>(BATCH_SIZE);
        for (int page = 0; selected.size() < BATCH_SIZE; page++) {
            List<DocDocument> candidates = docDocumentRepository.findNotIngestedBatch(
                    IngestStatus.NOT_INGESTED, PageRequest.of(page, QUERY_PAGE_SIZE));
            for (DocDocument candidate : candidates) {
                if (failureCounts.getOrDefault(candidate.getId(), 0) < MAX_FAILURES) {
                    selected.add(candidate);
                    if (selected.size() == BATCH_SIZE) break;
                }
            }
            if (candidates.size() < QUERY_PAGE_SIZE) break;
        }
        return selected;
    }

    private void processOne(Long documentId) {
        try {
            // Không fast-skip format ở worker: ingest() là nguồn sự thật duy nhất và tự persist
            // UNSUPPORTED_FORMAT mà không gọi Gemini.
            IngestResponseDTO result = docEmbeddingService.ingest(documentId);
            if ("INGESTED".equals(result.getStatus()) || "UNSUPPORTED_FORMAT".equals(result.getStatus())
                    || "EMPTY".equals(result.getStatus())) {
                failureCounts.remove(documentId);
                log.info("Auto-ingest document={} status={} chunks={}.",
                        documentId, result.getStatus(), result.getChunkCount());
                return;
            }

            // FILE_ERROR/NO_FILE vẫn NOT_INGESTED nên phải giới hạn retry để không chiếm batch mãi.
            recordFailure(documentId, "status=" + result.getStatus() + ", " + result.getMessage(), null);
        }
        catch (RuntimeException ex) {
            recordFailure(documentId, ex.getMessage(), ex);
        }
    }

    private void recordFailure(Long documentId, String reason, RuntimeException exception) {
        int attempts = failureCounts.merge(documentId, 1, Integer::sum);
        if (exception == null) {
            log.warn("Auto-ingest document={} thất bại lần {}/{}: {}",
                    documentId, attempts, MAX_FAILURES, reason);
        }
        else {
            log.error("Auto-ingest document={} thất bại lần {}/{}: {}",
                    documentId, attempts, MAX_FAILURES, reason, exception);
        }
        if (attempts >= MAX_FAILURES) {
            log.error("Auto-ingest tạm ngừng retry document={} cho tới khi ứng dụng restart.", documentId);
        }
    }

    private boolean sleepBetweenDocuments() {
        try {
            Thread.sleep(BETWEEN_DOCUMENT_DELAY_MS);
            return true;
        }
        catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            log.info("Auto-ingest bị ngắt trong throttle delay; kết thúc cycle để shutdown.");
            return false;
        }
    }
}
