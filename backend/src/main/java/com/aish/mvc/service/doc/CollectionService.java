package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.AddDocumentsResultDTO;
import com.aish.mvc.dto.doc.CollectionDetailResponseDTO;
import com.aish.mvc.dto.doc.CollectionResponseDTO;

import java.util.List;

/**
 * Bộ sưu tập tài liệu — "thư mục ảo" riêng của từng người dùng để gom tài liệu theo chủ đề.
 *
 * <p>Mọi bộ sưu tập đều riêng tư: các thao tác đều tính theo user đang đăng nhập, và bộ sưu tập
 * của người khác coi như không tồn tại (404). Bộ sưu tập chỉ chứa THAM CHIẾU tới tài liệu, nên
 * xoá bộ sưu tập hay gỡ tài liệu khỏi nó KHÔNG xoá tài liệu gốc.
 */
public interface CollectionService {

    /** Tạo bộ sưu tập mới. Tên phải qua kiểm duyệt và không trùng bộ sưu tập khác của cùng user. */
    CollectionResponseDTO createCollection(String name);

    /** Bộ sưu tập của user đang đăng nhập, mới nhất trước, kèm số tài liệu mỗi bộ. */
    List<CollectionResponseDTO> getMyCollections();

    /**
     * Chi tiết bộ sưu tập kèm danh sách tài liệu bên trong. Tài liệu không còn xem được (đã xoá,
     * chủ chuyển riêng tư, bị gỡ chia sẻ) vẫn hiện nhưng được đánh dấu "không khả dụng".
     */
    CollectionDetailResponseDTO getCollectionDetail(Long id);

    /** Đổi tên bộ sưu tập. Tên mới cũng phải qua kiểm duyệt và không được trùng. */
    CollectionResponseDTO renameCollection(Long id, String name);

    /** Xoá bộ sưu tập (và các liên kết bên trong). Tài liệu gốc KHÔNG bị xoá. */
    void deleteCollection(Long id);

    /**
     * Thêm nhiều tài liệu cùng lúc.
     *
     * @return kết quả chia 3 nhóm: đã thêm / vốn đã có / bỏ qua vì không có quyền xem — để FE
     *         báo rõ cho người dùng thay vì báo lỗi cả lô
     */
    AddDocumentsResultDTO addDocuments(Long id, List<Long> documentIds);

    /** Gỡ một tài liệu khỏi bộ sưu tập. Chỉ xoá liên kết, tài liệu gốc vẫn còn. */
    void removeDocument(Long id, Long documentId);
}
