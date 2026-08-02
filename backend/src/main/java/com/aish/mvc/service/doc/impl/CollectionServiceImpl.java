package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.AddDocumentsResultDTO;
import com.aish.mvc.dto.doc.CollectionDetailResponseDTO;
import com.aish.mvc.dto.doc.CollectionItemDTO;
import com.aish.mvc.dto.doc.CollectionResponseDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.Collection;
import com.aish.mvc.entity.doc.CollectionItem;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.CollectionItemRepository;
import com.aish.mvc.repository.doc.CollectionRepository;
import com.aish.mvc.service.doc.CollectionService;
import com.aish.mvc.service.doc.DocumentAccessPort;
import com.aish.mvc.service.doc.NamingModerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * BỘ SƯU TẬP tài liệu — cho phép user tự gom các tài liệu vào các "thư mục ảo" của riêng mình
 * (tạo, đổi tên, xoá bộ sưu tập; thêm/gỡ tài liệu khỏi bộ sưu tập).
 *
 * <p>Hai điểm cần nhớ: (1) mọi bộ sưu tập đều RIÊNG TƯ của người tạo — mọi thao tác đều kiểm tra
 * bộ sưu tập thuộc user đang đăng nhập; (2) bộ sưu tập chỉ CHỨA THAM CHIẾU tới tài liệu, nên xoá
 * bộ sưu tập hay gỡ tài liệu khỏi đó KHÔNG hề xoá tài liệu gốc.
 */
@Service
public class CollectionServiceImpl implements CollectionService {

    @Autowired private CollectionRepository collectionRepository;
    @Autowired private CollectionItemRepository collectionItemRepository;
    @Autowired private AuthAccountRepository authAccountRepository;
    @Autowired private DocumentAccessPort documentAccessPort;
    @Autowired private NamingModerationService namingModerationService;

    // Sao chép đúng pattern getCurrentUser() của DocumentServiceImpl.
    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    // Lấy collection và đảm bảo nó thuộc user hiện tại (nếu không -> 404).
    private Collection getOwnedCollection(Long id, Long userId) {
        return collectionRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bộ sưu tập!"));
    }

    /**
     * TẠO bộ sưu tập mới. Đầu vào: tên. Trả về: bộ sưu tập vừa tạo (số tài liệu = 0).
     * Các bước: (1) kiểm duyệt tên, (2) chặn trùng tên trong cùng một user, (3) lưu xuống DB.
     */
    @Override
    @Transactional
    public CollectionResponseDTO createCollection(String name) {
        AuthUser user = getCurrentUser();
        // Tên phải đủ dài, không toàn số/ký tự lặp, không chứa từ cấm.
        String cleaned = namingModerationService.validate(name);
        // Cùng một user không được có 2 bộ sưu tập trùng tên (người khác trùng tên thì không sao).
        if (collectionRepository.existsByUser_IdAndName(user.getId(), cleaned)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error.collection.duplicateName");
        }
        Collection c = new Collection();
        c.setName(cleaned);
        c.setUser(user);
        Collection saved = collectionRepository.save(c);
        return toResponseDTO(saved, 0L);
    }

    /** Danh sách bộ sưu tập của user đang đăng nhập, mới nhất trước, kèm số tài liệu mỗi bộ. */
    @Override
    @Transactional(readOnly = true)
    public List<CollectionResponseDTO> getMyCollections() {
        Long uid = getCurrentUser().getId();
        return collectionRepository.findByUser_IdOrderByCreatedAtDesc(uid).stream()
                .map(c -> toResponseDTO(c, collectionItemRepository.countByCollection_Id(c.getId())))
                .collect(Collectors.toList());
    }

    /**
     * CHI TIẾT một bộ sưu tập: thông tin bộ sưu tập + danh sách tài liệu bên trong.
     *
     * <p>Mỗi tài liệu được kiểm tra lại quyền xem tại thời điểm mở: tài liệu đã bị xoá hoặc chủ
     * nó đã chuyển sang riêng tư thì vẫn hiện trong danh sách nhưng đánh dấu "không khả dụng"
     * và không kèm nội dung.
     */
    @Override
    @Transactional(readOnly = true)
    public CollectionDetailResponseDTO getCollectionDetail(Long id) {
        Long uid = getCurrentUser().getId();
        Collection c = getOwnedCollection(id, uid);

        List<CollectionItemDTO> items = collectionItemRepository.findByCollection_IdOrderByAddedAtAsc(id).stream()
                .map(item -> toItemDTO(item, uid))
                .collect(Collectors.toList());

        CollectionDetailResponseDTO dto = new CollectionDetailResponseDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        dto.setItems(items);
        return dto;
    }

    /**
     * ĐỔI TÊN bộ sưu tập. Đầu vào: id + tên mới. Trả về: bộ sưu tập sau khi đổi.
     * Tên mới cũng phải qua kiểm duyệt và không được trùng bộ sưu tập khác của cùng user.
     */
    @Override
    @Transactional
    public CollectionResponseDTO renameCollection(Long id, String name) {
        Long uid = getCurrentUser().getId();
        Collection c = getOwnedCollection(id, uid);
        String cleaned = namingModerationService.validate(name);
        if (!cleaned.equals(c.getName()) && collectionRepository.existsByUser_IdAndName(uid, cleaned)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error.collection.duplicateName");
        }
        c.setName(cleaned);
        Collection saved = collectionRepository.save(c);
        return toResponseDTO(saved, collectionItemRepository.countByCollection_Id(saved.getId()));
    }

    /**
     * XOÁ bộ sưu tập. Đầu vào: id. Chỉ xoá bộ sưu tập và các dòng liên kết bên trong —
     * TÀI LIỆU GỐC KHÔNG bị xoá.
     */
    @Override
    @Transactional
    public void deleteCollection(Long id) {
        Long uid = getCurrentUser().getId();
        Collection c = getOwnedCollection(id, uid);
        // Cascade + orphanRemoval xóa CollectionItem của nó. KHÔNG đụng DocDocument.
        collectionRepository.delete(c);
    }

    /**
     * THÊM NHIỀU TÀI LIỆU vào bộ sưu tập cùng lúc.
     *
     * <p>Đầu vào: id bộ sưu tập + danh sách id tài liệu. Trả về: kết quả chia làm 3 nhóm — đã
     * thêm, vốn đã có sẵn, bị bỏ qua vì không có quyền. Nhờ vậy FE báo được cho người dùng biết
     * chính xác cái nào vào được cái nào không, thay vì báo lỗi cả lô.
     */
    @Override
    @Transactional
    public AddDocumentsResultDTO addDocuments(Long id, List<Long> documentIds) {
        Long uid = getCurrentUser().getId();
        Collection c = getOwnedCollection(id, uid);

        AddDocumentsResultDTO result = new AddDocumentsResultDTO();
        if (documentIds == null) return result;

        for (Long docId : documentIds) {
            if (docId == null) continue;

            // Đã có sẵn (kể cả vừa thêm trong cùng request) -> không thêm bản 2.
            if (result.getAdded().contains(docId)
                    || collectionItemRepository.existsByCollection_IdAndDocumentId(id, docId)) {
                if (!result.getAlreadyPresent().contains(docId)) result.getAlreadyPresent().add(docId);
                continue;
            }

            // Chỉ add doc user đang có quyền xem lúc add.
            if (!documentAccessPort.canAddToCollection(docId, uid)) {
                result.getSkippedNoPermission().add(docId);
                continue;
            }

            CollectionItem item = new CollectionItem();
            item.setCollection(c);
            item.setDocumentId(docId);
            // Ghi xuống database (bảng collection_items): chỉ lưu "bộ sưu tập nào chứa tài liệu
            // nào", không sao chép nội dung tài liệu.
            collectionItemRepository.save(item);
            result.getAdded().add(docId);
        }
        return result;
    }

    /**
     * GỠ một tài liệu khỏi bộ sưu tập. Đầu vào: id bộ sưu tập + id tài liệu.
     * Chỉ xoá dòng liên kết — tài liệu gốc vẫn còn nguyên trong "Tài liệu của tôi".
     */
    @Override
    @Transactional
    public void removeDocument(Long id, Long documentId) {
        Long uid = getCurrentUser().getId();
        getOwnedCollection(id, uid); // xác nhận collection thuộc user hiện tại
        CollectionItem item = collectionItemRepository.findByCollection_IdAndDocumentId(id, documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tài liệu không có trong bộ sưu tập!"));
        // Gỡ khỏi collection, KHÔNG xóa DocDocument.
        collectionItemRepository.delete(item);
    }

    private CollectionResponseDTO toResponseDTO(Collection c, Long documentCount) {
        CollectionResponseDTO dto = new CollectionResponseDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setDocumentCount(documentCount);
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }

    private CollectionItemDTO toItemDTO(CollectionItem item, Long currentUserId) {
        CollectionItemDTO dto = new CollectionItemDTO();
        dto.setDocumentId(item.getDocumentId());
        dto.setAddedAt(item.getAddedAt());

        boolean available = documentAccessPort.isAvailableTo(item.getDocumentId(), currentUserId);
        dto.setAvailable(available);
        // available=false -> KHÔNG kèm document (Jackson bỏ field null). available=true -> kèm DTO đầy đủ.
        if (available) {
            documentAccessPort.getDocumentDto(item.getDocumentId(), currentUserId).ifPresent(dto::setDocument);
        }
        return dto;
    }
}
