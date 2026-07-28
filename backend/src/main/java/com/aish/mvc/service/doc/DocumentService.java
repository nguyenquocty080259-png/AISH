package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.AdminDocumentSummaryDTO;
import com.aish.mvc.dto.doc.CommunityPageResponseDTO;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.dto.doc.StorageUsageDTO;
import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.entity.enums.DocumentVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Nghiệp vụ tài liệu: tải lên, đọc, sửa, thùng rác và chuyển công khai.
 *
 * <p><b>Luật truy cập dùng chung</b> cho mọi lối đọc nội dung ({@link #getDocumentById(Long)},
 * {@link #getFileForPreview(Long)}, {@link #getFileByDocumentId(Long)}): chủ sở hữu, hoặc tài
 * liệu PUBLIC, hoặc người đang được chia sẻ (xem {@link DocumentShareService#hasShareAccess}).
 * Sai quyền ném {@link com.aish.mvc.exception.ForbiddenException} (403). Tài liệu đã xoá mềm
 * coi như không tồn tại với mọi người, kể cả chủ sở hữu, và được kiểm tra TRƯỚC quyền để 403
 * không lộ ra rằng id đó từng tồn tại.
 *
 * <p>Mọi method đều thao tác theo user đang đăng nhập lấy từ SecurityContext — không nhận
 * userId từ client.
 */
public interface DocumentService {

    /** Tài liệu của user đang đăng nhập, chưa xoá mềm (trang "Tài liệu của tôi"). */
    List<DocumentResponseDTO> getAllDocuments();

    /**
     * Trang Cộng đồng: chỉ tài liệu PUBLIC, có tìm kiếm/lọc/sắp xếp/phân trang.
     *
     * @param keyword   từ khoá tìm trong tiêu đề/mô tả; null hoặc rỗng là không lọc
     * @param subjectId lọc theo môn học; null là mọi môn
     * @param minRating điểm trung bình tối thiểu; null là không lọc
     * @param sortBy    "downloads" | "rating"; giá trị khác rơi về mới nhất trước
     * @param page      số trang tính từ 0; số âm được kẹp về 0
     * @param size      số tài liệu mỗi trang; <= 0 rơi về mặc định, vượt trần thì bị kẹp lại
     */
    CommunityPageResponseDTO getCommunityDocuments(String keyword, Long subjectId, Double minRating, String sortBy, int page, int size);

    /**
     * File để XEM TRƯỚC (inline) — dùng chung cho /preview, /thumbnail và /preview-text.
     * Không ghi nhận lượt tải. Ưu tiên bản local khi tài liệu lưu ở cả hai nơi.
     *
     * @throws com.aish.mvc.exception.ResourceNotFoundException tài liệu không tồn tại, hoặc
     *         tồn tại nhưng chưa gắn file nào
     * @throws com.aish.mvc.exception.ForbiddenException không thoả luật truy cập chung
     */
    DocFile getFileForPreview(Long id);

    /** Tải lên và chỉ lưu trên đĩa máy chủ. Tương đương {@link #uploadDocument} với LOCAL. */
    DocumentResponseDTO uploadDocumentToServer(String title, String description, java.util.List<Long> subjectIds, MultipartFile file);

    /** Tải lên và chỉ lưu trên Cloudinary. Tương đương {@link #uploadDocument} với CLOUD. */
    DocumentResponseDTO uploadDocumentToCloud(String title, String description, java.util.List<Long> subjectIds, MultipartFile file);

    /**
     * Tải tài liệu lên. Tài liệu mới luôn ở chế độ PRIVATE (DEC-006) — muốn công khai thì gọi
     * {@link #toggleVisibility(Long)} để đi qua kiểm duyệt.
     *
     * <p>Thứ tự chốt chặn: tuổi tối thiểu, loại tệp (allowlist đuôi + đối chiếu content-type
     * thật), dung lượng một tệp, quota tổng của user, và tài liệu phải thuộc ít nhất một môn
     * học hợp lệ. Với BOTH, giới hạn dung lượng lẫn quota phải qua CẢ HAI phía.
     *
     * @param storage {@link StorageTarget#LOCAL}, {@link StorageTarget#CLOUD} hoặc
     *                {@link StorageTarget#BOTH} (BOTH lưu 2 bản, mọi lối đọc ưu tiên local)
     * @throws IllegalArgumentException storage không hợp lệ, hoặc subjectIds rỗng/chứa id
     *         môn học không tồn tại
     */
    DocumentResponseDTO uploadDocument(String title, String description, java.util.List<Long> subjectIds, MultipartFile file, String storage);

    /**
     * Sửa metadata tài liệu — chỉ chủ sở hữu, và tài liệu không được nằm trong thùng rác.
     *
     * @param subjectIds null là giữ nguyên danh sách môn học hiện tại; nếu gửi thì mọi id phải
     *                   tồn tại, id lạ làm cả thao tác thất bại thay vì bị bỏ qua
     */
    DocumentResponseDTO updateDocument(Long id, String title, String description, java.util.List<Long> subjectIds);

    /** Trang "Yêu thích" của user đang đăng nhập — đã loại tài liệu trong thùng rác. */
    List<DocumentResponseDTO> getFavoriteDocuments();

    /**
     * Dung lượng đã dùng + giới hạn hiện hành của user đang đăng nhập, cho form upload
     * (tiền-kiểm phía FE) và thanh dung lượng. Dung lượng đã dùng TÍNH CẢ tài liệu trong thùng
     * rác vì bytes vẫn chiếm chỗ tới khi xoá vĩnh viễn.
     */
    StorageUsageDTO getStorageUsage();

    /** Xoá mềm (đưa vào thùng rác) — chỉ chủ sở hữu. File trên đĩa/Cloudinary vẫn còn. */
    void deleteDocument(Long id);

    /** Thùng rác của user đang đăng nhập. */
    List<DocumentResponseDTO> getDeletedDocuments();

    /** Khôi phục tài liệu khỏi thùng rác — chỉ chủ sở hữu. */
    void restoreDocument(Long id);

    /**
     * Xoá vĩnh viễn — chỉ chủ sở hữu. Xoá file trên đĩa/Cloudinary, thumbnail, và mọi dữ liệu
     * liên quan (bình luận, đánh giá, yêu thích, lượt tải, embedding, kháng cáo, chia sẻ...).
     */
    void permanentDeleteDocument(Long id);

    /**
     * File để TẢI XUỐNG. Cùng luật truy cập với {@link #getFileForPreview(Long)} — tải cũng là
     * một kênh lấy nội dung. Ưu tiên bản local khi tài liệu lưu ở cả hai nơi.
     *
     * @throws com.aish.mvc.exception.ResourceNotFoundException tài liệu không tồn tại hoặc
     *         chưa gắn file nào
     * @throws com.aish.mvc.exception.ForbiddenException không thoả luật truy cập chung
     */
    DocFile getFileByDocumentId(Long documentId);

    /**
     * Đảo chế độ hiển thị — chỉ chủ sở hữu. PUBLIC -> PRIVATE luôn được phép. Chiều ngược lại
     * phải qua kiểm duyệt: trúng từ khoá cấm thì bị từ chối ngay (không gọi AI), nếu không thì
     * qua AI pre-screen (DEC-035); FLAG cũng bị coi là REJECTED và tài liệu ở lại PRIVATE.
     *
     * @return tài liệu sau khi đổi, để FE đọc visibility/moderationStatus/moderationReason mới
     */
    DocumentResponseDTO toggleVisibility(Long documentId);

    /**
     * Chi tiết tài liệu theo luật truy cập chung của interface này.
     *
     * @throws com.aish.mvc.exception.ResourceNotFoundException không tồn tại hoặc đã xoá mềm
     * @throws com.aish.mvc.exception.ForbiddenException không thoả luật truy cập chung
     */
    DocumentResponseDTO getDocumentById(Long id);

    // --- Admin oversight (DEC-009: admin xem/gỡ được mọi tài liệu nhưng KHÔNG trở thành owner) ---

    /**
     * Mọi tài liệu bất kể owner/visibility — KHÔNG lọc theo quyền xem của user gọi.
     * Quyền ADMIN được gate ở tầng route, service không kiểm tra lại.
     *
     * @param visibility  lọc theo chế độ hiển thị; null là mọi chế độ (bị bỏ qua khi needsReview)
     * @param needsReview true là chỉ lấy hàng chờ duyệt: đã qua kiểm duyệt AI nhưng admin chưa
     *                    xem lại (adminReviewedAt còn trống), và chưa bị xoá mềm
     */
    Page<AdminDocumentSummaryDTO> getAllDocumentsForAdmin(
            DocumentVisibility visibility,
            boolean needsReview,
            Pageable pageable
    );

    /** Admin gỡ tài liệu (xoá mềm) — không kiểm tra ownership, document.user giữ nguyên. */
    void adminDeleteDocument(Long id);

    /** Admin sửa metadata — như {@link #updateDocument} nhưng không kiểm tra ownership. */
    DocumentResponseDTO adminUpdateDocument(Long id, String title, String description, java.util.List<Long> subjectIds);

    /** Admin khôi phục tài liệu đã gỡ — không kiểm tra ownership. */
    void adminRestoreDocument(Long id);
}
