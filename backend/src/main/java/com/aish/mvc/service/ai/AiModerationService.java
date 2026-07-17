package com.aish.mvc.service.ai;

import com.aish.mvc.dto.ai.ModerationResultDTO;
import com.aish.mvc.entity.doc.DocDocument;

public interface AiModerationService {

    // Kiểm duyệt 1 tài liệu bằng AI (1 lệnh gọi Groq): PASS -> đủ điều kiện public,
    // FLAG -> cần Admin duyệt thủ công. Fail-safe: mọi lỗi/không rõ ràng -> FLAG.
    ModerationResultDTO screen(Long documentId);

    ModerationResultDTO screenText(String text);

    MetadataMatchResult checkMetadata(DocDocument document);

    record MetadataMatchResult(String status, String reason) {
        public boolean isMismatch() {
            return "LECH".equals(status);
        }
    }
}
