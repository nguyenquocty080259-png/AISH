package com.aish.mvc.service.doc;

/**
 * Tương tác của người dùng với tài liệu: bình luận, yêu thích, đánh giá, và ghi nhận lượt
 * xem/lượt tải.
 *
 * <p>Bình luận, yêu thích và đánh giá là thao tác TRÊN nội dung nên chỉ hợp lệ khi người dùng
 * thật sự được xem tài liệu đó — luật quyền lấy từ {@link DocumentAccessPort#isAvailableTo}
 * (chủ sở hữu / PUBLIC / được chia sẻ, và tài liệu chưa bị xoá mềm). Không thoả thì ném
 * {@link com.aish.mvc.exception.ForbiddenException}; tài liệu không tồn tại thì ném
 * {@link com.aish.mvc.exception.ResourceNotFoundException}.
 */
public interface EngagementService {

    /**
     * Thêm bình luận. Nội dung bị lọc từ khoá kiểm duyệt trước khi lưu: trúng từ khoá thì mặc
     * định BỊ CHẶN ({@link com.aish.mvc.exception.CommentBlockedException}); người dùng có thể
     * gửi lại với {@code dispute = true} kèm lý do để bình luận được lưu ở trạng thái
     * PENDING_REVIEW chờ admin duyệt. Bình luận sạch vào thẳng VISIBLE.
     *
     * @param dispute     người dùng chủ động khiếu nại quyết định chặn
     * @param disputeNote lý do khiếu nại — bắt buộc khi {@code dispute = true}
     * @throws IllegalArgumentException nội dung rỗng, hoặc khiếu nại mà không nêu lý do
     */
    void addComment(Long documentId, String content, boolean dispute, String disputeNote);

    /** Bật/tắt yêu thích trên cùng một endpoint: chưa yêu thích thì thêm, đã yêu thích thì bỏ. */
    void toggleFavorite(Long documentId);

    /**
     * Chấm điểm tài liệu. Mỗi user một điểm cho mỗi tài liệu — chấm lại là ghi đè điểm cũ,
     * không cộng thêm bản ghi.
     *
     * @param star số nguyên 1..5
     * @throws IllegalArgumentException star null hoặc nằm ngoài khoảng 1..5
     */
    void rateDocument(Long documentId, Integer star);

    /**
     * Ghi nhận một lượt tải. Người gọi (controller) chỉ được gọi SAU khi chắc chắn có file
     * thật để trả về, nếu không downloadCount bị thổi lên bởi những lần tải hỏng.
     */
    void logDownload(Long documentId);

    /**
     * Ghi nhận lượt xem vào lịch sử xem. Mỗi cặp (user, tài liệu) chỉ có một bản ghi — xem lại
     * là cập nhật thời điểm, không thêm dòng mới. Chỉ gọi sau khi qua kiểm tra quyền.
     */
    void logView(Long documentId);

    /**
     * Sửa bình luận — chỉ tác giả. Nội dung mới bị lọc từ khoá lại như lúc thêm mới, và trạng
     * thái duyệt trước đó bị đặt lại.
     *
     * @throws com.aish.mvc.exception.ForbiddenException người gọi không phải tác giả
     */
    void updateComment(Long commentId, String content, boolean dispute, String disputeNote);

    /**
     * Xoá bình luận — chỉ tác giả.
     *
     * @throws com.aish.mvc.exception.ForbiddenException người gọi không phải tác giả
     */
    void deleteComment(Long commentId);
}
