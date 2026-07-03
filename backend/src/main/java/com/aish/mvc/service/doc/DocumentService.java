package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.AdminDocumentSummaryDTO;
import com.aish.mvc.dto.doc.CommunityPageResponseDTO;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.doc.DocFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface DocumentService {

    List<DocumentResponseDTO> getAllDocuments();

    // Trang Cộng đồng: chỉ tài liệu PUBLIC, có tìm kiếm/lọc/sắp xếp/phân trang
    CommunityPageResponseDTO getCommunityDocuments(String keyword, Long subjectId, String sortBy, int page, int size);

    // Trả về file để XEM TRƯỚC (inline) - cho phép nếu doc PUBLIC, hoặc PRIVATE nhưng là chủ sở hữu.
    // Không ghi log lượt tải như download.
    DocFile getFileForPreview(Long id);

    DocumentResponseDTO uploadDocumentToServer(String title, String description, java.util.List<Long> subjectIds, MultipartFile file);

    DocumentResponseDTO uploadDocumentToCloud(String title, String description, java.util.List<Long> subjectIds, MultipartFile file);

    void addComment(Long documentId, String content);

    void toggleFavorite(Long documentId);

    // Trang "Yêu thích" của user đang đăng nhập — loại tài liệu đã bị xoá mềm (thùng rác).
    List<DocumentResponseDTO> getFavoriteDocuments();

    void rateDocument(Long documentId, Integer star);

    void logDownload(Long documentId);

    void deleteDocument(Long id);

    List<DocumentResponseDTO> getDeletedDocuments();

    void restoreDocument(Long id);

    void permanentDeleteDocument(Long id);

    DocFile getFileByDocumentId(Long documentId);

    void toggleVisibility(Long documentId);

    DocumentResponseDTO getDocumentById(Long id);

    // --- Admin oversight (DEC-009: admin xem/gỡ được mọi tài liệu nhưng KHÔNG trở thành owner) ---

    // Mọi tài liệu, bất kỳ owner/visibility nào — không phải danh sách theo quyền xem của user.
    Page<AdminDocumentSummaryDTO> getAllDocumentsForAdmin(Pageable pageable);

    // Admin takedown (soft-delete) — không kiểm tra ownership, chỉ cần role ADMIN (gate ở route).
    void adminDeleteDocument(Long id);
}