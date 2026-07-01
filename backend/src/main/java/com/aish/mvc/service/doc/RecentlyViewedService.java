package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.RecentlyViewedItemDTO;

import java.util.List;

public interface RecentlyViewedService {

    // Ghi/cập nhật lượt xem của user hiện tại cho document. Trả false nếu doc không hợp lệ (bỏ qua).
    boolean recordView(Long documentId);

    // Doc xem gần đây (mới nhất trước), chỉ doc còn khả dụng, tối đa 20.
    List<RecentlyViewedItemDTO> getRecentlyViewed();
}
