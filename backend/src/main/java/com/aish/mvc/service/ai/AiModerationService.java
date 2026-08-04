package com.aish.mvc.service.ai;

import com.aish.mvc.dto.ai.ModerationResultDTO;
import com.aish.mvc.entity.doc.DocDocument;

/**
 * Gọi AI để KIỂM DUYỆT nội dung: tài liệu xin công khai, tin nhắn chat/bình luận, và đối chiếu
 * metadata (tiêu đề/môn học) có khớp nội dung thật hay không. Mọi quyết định đều "fail-safe":
 * lỗi/không rõ ràng thì nghiêng về phía an toàn hơn (FLAG cho tài liệu, PASS cho chat để tránh
 * chặn oan người dùng — xem cài đặt cụ thể ở AiModerationServiceImpl).
 */
public interface AiModerationService {

    // Kiểm duyệt 1 tài liệu bằng AI (1 lệnh gọi Groq): PASS -> đủ điều kiện public,
    // FLAG -> cần Admin duyệt thủ công. Fail-safe: mọi lỗi/không rõ ràng -> FLAG.
    ModerationResultDTO screen(Long documentId);

    // Kiểm duyệt một đoạn văn bản rời (tin nhắn chat, bình luận) — không gắn với tài liệu cụ thể.
    ModerationResultDTO screenText(String text);

    // Kiểm tra riêng phần metadata (tiêu đề/mô tả/môn học) có khớp với nội dung thật của tài liệu không.
    MetadataMatchResult checkMetadata(DocDocument document);

    // Kết quả đối chiếu metadata: status = "KHOP" (khớp) hoặc "LECH" (lệch), kèm lý do.
    record MetadataMatchResult(String status, String reason) {
        public boolean isMismatch() {
            return "LECH".equals(status);
        }
    }
}
