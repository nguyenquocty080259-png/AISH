package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.RecentlyViewedItemDTO;

import java.util.List;

/**
 * Lịch sử xem gần đây — dữ liệu cho mục "Tiếp tục học". Mỗi cặp (người xem, tài liệu) chỉ giữ
 * một dòng nên danh sách không bị lặp; tài liệu không còn xem được thì tự động bị ẩn khỏi danh sách.
 */
public interface RecentlyViewedService {

    // Ghi/cập nhật lượt xem của user hiện tại cho document. Trả false nếu doc không hợp lệ (bỏ qua).
    boolean recordView(Long documentId);

    // Doc xem gần đây (mới nhất trước), chỉ doc còn khả dụng, tối đa 20.
    List<RecentlyViewedItemDTO> getRecentlyViewed();
}
