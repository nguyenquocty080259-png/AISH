package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.RecentlyViewedItemDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.ViewHistory;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.ViewHistoryRepository;
import com.aish.mvc.service.doc.DocumentAccessPort;
import com.aish.mvc.service.doc.RecentlyViewedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * LỊCH SỬ XEM GẦN ĐÂY — dữ liệu cho mục "Tiếp tục học" ở trang chủ: ghi lại user vừa xem tài
 * liệu nào và liệt kê lại theo thứ tự mới nhất trước.
 *
 * <p>Mỗi cặp (người xem, tài liệu) chỉ giữ MỘT dòng: xem lại thì chỉ cập nhật thời điểm, nên
 * danh sách không bị lặp đi lặp lại một tài liệu.
 */
@Service
public class RecentlyViewedServiceImpl implements RecentlyViewedService {

    // Chỉ hiện tối đa 20 tài liệu gần nhất cho gọn giao diện.
    private static final int LIMIT = 20;

    @Autowired private ViewHistoryRepository viewHistoryRepository;
    @Autowired private AuthAccountRepository authAccountRepository;
    @Autowired private DocDocumentRepository docDocumentRepository;
    @Autowired private DocumentAccessPort documentAccessPort;

    // Sao chép đúng pattern getCurrentUser() của CollectionServiceImpl.
    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    /**
     * GHI NHẬN một lượt xem. Đầu vào: id tài liệu. Trả về: true nếu đã ghi, false nếu bỏ qua.
     *
     * <p>Các bước: (1) đã có dòng cho cặp (user, tài liệu) thì chỉ cập nhật thời điểm xem;
     * (2) chưa có thì chỉ tạo mới khi tài liệu còn tồn tại và chưa bị xoá — tài liệu không hợp
     * lệ thì bỏ qua im lặng (người dùng không cần biết, và cũng không lộ tài liệu có tồn tại hay không).
     */
    @Override
    @Transactional
    public boolean recordView(Long documentId) {
        AuthUser user = getCurrentUser();

        // Upsert: đã có (user, documentId) -> CHỈ update viewedAt = now, không tạo dòng mới.
        ViewHistory existing = viewHistoryRepository
                .findByUser_IdAndDocumentId(user.getId(), documentId)
                .orElse(null);
        if (existing != null) {
            existing.setViewedAt(LocalDateTime.now());
            viewHistoryRepository.save(existing);
            return true;
        }

        // Chưa có -> chỉ tạo nếu doc tồn tại và chưa bị xóa mềm. Ngược lại bỏ qua im lặng.
        DocDocument doc = docDocumentRepository.findById(documentId).orElse(null);
        if (doc == null || doc.getDeletedAt() != null) {
            return false;
        }

        ViewHistory vh = ViewHistory.builder()
                .user(user)
                .documentId(documentId)
                .viewedAt(LocalDateTime.now())
                .build();
        viewHistoryRepository.save(vh);
        return true;
    }

    /**
     * DANH SÁCH TÀI LIỆU XEM GẦN ĐÂY, mới nhất trước, tối đa 20.
     *
     * <p>Mỗi dòng lịch sử đều được kiểm tra lại quyền xem tại thời điểm hiện tại: tài liệu đã bị
     * xoá, chủ nó chuyển sang riêng tư, hoặc mình bị gỡ chia sẻ thì ẨN LUÔN khỏi danh sách.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RecentlyViewedItemDTO> getRecentlyViewed() {
        Long uid = getCurrentUser().getId();

        List<RecentlyViewedItemDTO> result = new ArrayList<>();
        for (ViewHistory vh : viewHistoryRepository.findByUser_IdOrderByViewedAtDesc(uid)) {
            if (result.size() >= LIMIT) break;

            // TÁI DÙNG DocumentAccessPort của Collections — không viết lại logic availability.
            // Doc không còn khả dụng (đã xóa / chủ chuyển private / mất quyền) -> ẨN LUÔN (bỏ khỏi list).
            if (!documentAccessPort.isAvailableTo(vh.getDocumentId(), uid)) continue;

            documentAccessPort.getDocumentDto(vh.getDocumentId(), uid).ifPresent(docDto -> {
                RecentlyViewedItemDTO dto = new RecentlyViewedItemDTO();
                dto.setDocumentId(vh.getDocumentId());
                dto.setViewedAt(vh.getViewedAt());
                dto.setDocument(docDto);
                result.add(dto);
            });
        }
        return result;
    }
}
