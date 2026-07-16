package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.AdminDocumentSummaryDTO;
import com.aish.mvc.dto.doc.CommunityPageResponseDTO;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.entity.enums.DocumentVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface DocumentService {

    List<DocumentResponseDTO> getAllDocuments();

    // Trang Cộng đồng: chỉ tài liệu PUBLIC, có tìm kiếm/lọc/sắp xếp/phân trang
    CommunityPageResponseDTO getCommunityDocuments(String keyword, Long subjectId, Long tagId, Double minRating, String sortBy, int page, int size);
    // Trả về file để XEM TRƯỚC (inline) - cho phép nếu doc PUBLIC, hoặc PRIVATE nhưng là chủ sở hữu.
    // Không ghi log lượt tải như download.
    DocFile getFileForPreview(Long id);

    DocumentResponseDTO uploadDocumentToServer(String title, String description, java.util.List<Long> subjectIds, MultipartFile file);

    DocumentResponseDTO uploadDocumentToCloud(String title, String description, java.util.List<Long> subjectIds, MultipartFile file);

    // Upload hợp nhất: storage = "LOCAL" | "CLOUD" | "BOTH" (BOTH lưu cả 2 nơi, ưu tiên local khi đọc).
    // Upload hợp nhất: storage = "LOCAL" | "CLOUD" | "BOTH" (BOTH lưu cả 2 nơi, ưu tiên local khi đọc).
    DocumentResponseDTO uploadDocument(String title, String description, java.util.List<Long> subjectIds, MultipartFile file, String storage);

    // Sửa metadata tài liệu (title/description/subjectIds). subjectIds == null -> giữ nguyên.
    DocumentResponseDTO updateDocument(Long id, String title, String description, java.util.List<Long> subjectIds);


    // Trang "Yêu thích" của user đang đăng nhập — loại tài liệu đã bị xoá mềm (thùng rác).
    List<DocumentResponseDTO> getFavoriteDocuments();





    void deleteDocument(Long id);

    List<DocumentResponseDTO> getDeletedDocuments();

    void restoreDocument(Long id);

    void permanentDeleteDocument(Long id);

    DocFile getFileByDocumentId(Long documentId);

    // Trả về document sau khi đổi để FE biết kết quả kiểm duyệt AI (visibility, moderationStatus, moderationReason).
    DocumentResponseDTO toggleVisibility(Long documentId);

    DocumentResponseDTO getDocumentById(Long id);

    // --- Admin oversight (DEC-009: admin xem/gỡ được mọi tài liệu nhưng KHÔNG trở thành owner) ---

    // Mọi tài liệu, bất kỳ owner/visibility nào — không phải danh sách theo quyền xem của user.
    Page<AdminDocumentSummaryDTO> getAllDocumentsForAdmin(
            DocumentVisibility visibility,
            boolean needsReview,
            Pageable pageable
    );

    // Admin takedown (soft-delete) — không kiểm tra ownership, chỉ cần role ADMIN (gate ở route).
    void adminDeleteDocument(Long id);

    // Admin sửa metadata (title/description/subjectIds) — logic giống updateDocument() nhưng
    // không kiểm tra ownership, chỉ cần role ADMIN (gate ở route).
    DocumentResponseDTO adminUpdateDocument(Long id, String title, String description, java.util.List<Long> subjectIds);

    // Admin khôi phục tài liệu đã gỡ — logic giống restoreDocument() nhưng không kiểm tra
    // ownership, chỉ cần role ADMIN (gate ở route).
    void adminRestoreDocument(Long id);
}
