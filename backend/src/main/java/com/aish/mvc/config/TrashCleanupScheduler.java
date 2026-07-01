package com.aish.mvc.config;

import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.service.doc.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TrashCleanupScheduler {

    private final DocDocumentRepository docDocumentRepository;
    private final DocumentService documentService;

    // Chạy mỗi ngày lúc 3h sáng: xóa cứng tài liệu đã ở thùng rác quá 30 ngày
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldTrash() {
        LocalDateTime limit = LocalDateTime.now().minusDays(30);
        List<DocDocument> expired = docDocumentRepository.findByDeletedAtBefore(limit);
        for (DocDocument doc : expired) {
            documentService.permanentDeleteDocument(doc.getId());
        }
    }
}
