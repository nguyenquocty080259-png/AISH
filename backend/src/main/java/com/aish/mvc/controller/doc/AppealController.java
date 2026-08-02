package com.aish.mvc.controller.doc;

import com.aish.mvc.dto.doc.ModerationAppealResponseDTO;
import com.aish.mvc.service.doc.ModerationAppealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API xem KHÁNG CÁO KIỂM DUYỆT của chính người dùng (/api/appeals). Chỉ có phần XEM ở đây; việc
 * GỬI kháng cáo nằm ở {@code POST /api/documents/{id}/appeal} trong DocumentController, vì thao
 * tác đó gắn với một tài liệu cụ thể.
 */
@RestController
@RequestMapping("/api/appeals")
@RequiredArgsConstructor
public class AppealController {

    private final ModerationAppealService moderationAppealService;

    // Danh sách kháng cáo của user đang đăng nhập (mới nhất trước) để họ theo dõi tiến độ xử lý.
    @GetMapping("/mine")
    public ResponseEntity<List<ModerationAppealResponseDTO>> getMyAppeals() {
        return ResponseEntity.ok(moderationAppealService.listMyAppeals());
    }
}
