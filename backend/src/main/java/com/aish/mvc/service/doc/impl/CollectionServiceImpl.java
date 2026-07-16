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

    @Override
    @Transactional
    public CollectionResponseDTO createCollection(String name) {
        AuthUser user = getCurrentUser();
        String cleaned = namingModerationService.validate(name);
        if (collectionRepository.existsByUser_IdAndName(user.getId(), cleaned)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bạn đã có bộ sưu tập trùng tên!");
        }
        Collection c = new Collection();
        c.setName(cleaned);
        c.setUser(user);
        Collection saved = collectionRepository.save(c);
        return toResponseDTO(saved, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollectionResponseDTO> getMyCollections() {
        Long uid = getCurrentUser().getId();
        return collectionRepository.findByUser_IdOrderByCreatedAtDesc(uid).stream()
                .map(c -> toResponseDTO(c, collectionItemRepository.countByCollection_Id(c.getId())))
                .collect(Collectors.toList());
    }

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

    @Override
    @Transactional
    public CollectionResponseDTO renameCollection(Long id, String name) {
        Long uid = getCurrentUser().getId();
        Collection c = getOwnedCollection(id, uid);
        String cleaned = namingModerationService.validate(name);
        if (!cleaned.equals(c.getName()) && collectionRepository.existsByUser_IdAndName(uid, cleaned)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bạn đã có bộ sưu tập trùng tên!");
        }
        c.setName(cleaned);
        Collection saved = collectionRepository.save(c);
        return toResponseDTO(saved, collectionItemRepository.countByCollection_Id(saved.getId()));
    }

    @Override
    @Transactional
    public void deleteCollection(Long id) {
        Long uid = getCurrentUser().getId();
        Collection c = getOwnedCollection(id, uid);
        // Cascade + orphanRemoval xóa CollectionItem của nó. KHÔNG đụng DocDocument.
        collectionRepository.delete(c);
    }

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
            collectionItemRepository.save(item);
            result.getAdded().add(docId);
        }
        return result;
    }

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
