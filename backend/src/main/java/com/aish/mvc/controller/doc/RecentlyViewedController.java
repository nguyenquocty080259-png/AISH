package com.aish.mvc.controller.doc;

import com.aish.mvc.dto.doc.RecentlyViewedItemDTO;
import com.aish.mvc.service.doc.RecentlyViewedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API cho tính năng "TIẾP TỤC HỌC" (lịch sử xem gần đây): ghi nhận lượt xem và trả về danh sách
 * tài liệu vừa xem.
 */
// Controller RIÊNG cho tính năng "Tiếp tục học" — KHÔNG đụng DocumentController (Person 2)
// hay UserController/ProfileController (Person 1), dù path nằm dưới /api/documents và /api/users.
@RestController
@RequiredArgsConstructor
public class RecentlyViewedController {

    private final RecentlyViewedService recentlyViewedService;

    // Ghi/cập nhật lượt xem của user hiện tại. Doc không tồn tại/đã xóa -> bỏ qua im lặng, vẫn 204.
    @PostMapping("/api/documents/{id}/view")
    public ResponseEntity<Void> recordView(@PathVariable Long id) {
        recentlyViewedService.recordView(id);
        return ResponseEntity.noContent().build();
    }

    // Doc xem gần đây, mới nhất trước, tối đa 20, chỉ doc còn khả dụng.
    @GetMapping("/api/users/recently-viewed")
    public ResponseEntity<List<RecentlyViewedItemDTO>> getRecentlyViewed() {
        return ResponseEntity.ok(recentlyViewedService.getRecentlyViewed());
    }
}
