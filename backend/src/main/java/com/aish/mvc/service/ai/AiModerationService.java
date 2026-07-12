package com.aish.mvc.service.ai;

import com.aish.mvc.dto.ai.ModerationResultDTO;

public interface AiModerationService {

    // Kiểm duyệt 1 tài liệu bằng AI (1 lệnh gọi Groq): PASS -> đủ điều kiện public,
    // FLAG -> cần Admin duyệt thủ công. Fail-safe: mọi lỗi/không rõ ràng -> FLAG.
    ModerationResultDTO screen(Long documentId);

    ModerationResultDTO screenText(String text);
}
