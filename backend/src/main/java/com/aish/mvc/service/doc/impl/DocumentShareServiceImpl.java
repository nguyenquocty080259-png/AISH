package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.dto.doc.ShareRequestDTO;
import com.aish.mvc.dto.doc.ShareResponseDTO;
import com.aish.mvc.dto.doc.SharedWithMeItemDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.DocumentShare;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.NotificationType;
import com.aish.mvc.entity.enums.SharePermission;
import com.aish.mvc.entity.enums.ShareMode;
import com.aish.mvc.exception.ForbiddenException;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.DocumentShareRepository;
import com.aish.mvc.service.doc.DocumentShareService;
import com.aish.mvc.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentShareServiceImpl implements DocumentShareService {

    private final DocumentShareRepository documentShareRepository;
    private final DocDocumentRepository docDocumentRepository;
    private final AuthAccountRepository authAccountRepository;
    private final AuthUserRepository authUserRepository;
    private final NotificationService notificationService;
    private final DocumentMapper documentMapper;

    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    // Chỉ Author/Owner được thao tác share/revoke; tài liệu đã xóa mềm không share được.
    private DocDocument requireOwnedDocument(Long documentId, AuthUser owner) {
        DocDocument doc = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));
        if (doc.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Tài liệu không tồn tại!");
        }
        if (!doc.getUser().getId().equals(owner.getId())) {
            throw new ForbiddenException("Chỉ chủ sở hữu mới được chia sẻ tài liệu này!");
        }
        return doc;
    }

    @Override
    @Transactional
    public ShareResponseDTO shareDocument(Long documentId, ShareRequestDTO request) {
        AuthUser owner = getCurrentUser();
        DocDocument doc = requireOwnedDocument(documentId, owner);

        if (request == null || request.getMode() == null) {
            throw new IllegalArgumentException("Thiếu chế độ chia sẻ (mode).");
        }
        SharePermission permission = request.getPermission() == null
                ? SharePermission.VIEWER : request.getPermission();
        if (permission == SharePermission.EDITOR) {
            throw new IllegalArgumentException("Quyền EDITOR chưa được hỗ trợ ở phiên bản này.");
        }

        return switch (request.getMode()) {
            case RESTRICTED -> shareToUsers(doc, owner, request.getUserIds(), permission);
            case ANYONE_WITH_LINK -> shareByLink(doc, owner, permission);
            case NONE -> disableLinkShare(documentId);
        };
    }

    private ShareResponseDTO shareToUsers(
            DocDocument doc, AuthUser owner, List<Long> userIds, SharePermission permission) {
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("Cần chọn ít nhất một người để chia sẻ.");
        }
        String ownerName = owner.getFullName();
        for (Long userId : userIds) {
            if (userId == null || userId.equals(owner.getId())) continue; // bỏ qua chính chủ
            if (!authUserRepository.existsById(userId)) continue;          // bỏ qua user không tồn tại

            DocumentShare share = documentShareRepository
                    .findByDocumentIdAndSharedWithUserId(doc.getId(), userId)
                    .orElseGet(() -> DocumentShare.builder()
                            .documentId(doc.getId())
                            .sharedWithUserId(userId)
                            .sharedByUserId(owner.getId())
                            .build());
            share.setPermission(permission);
            share.setShareMode(ShareMode.RESTRICTED);
            share.setShareToken(null);
            documentShareRepository.save(share);

            notificationService.createDocumentNotification(
                    userId,
                    NotificationType.DOCUMENT_SHARED,
                    ownerName + " đã chia sẻ tài liệu \"" + doc.getTitle() + "\" với bạn.",
                    doc.getId());
        }

        doc.setVisibility(DocumentVisibility.SHARED);
        docDocumentRepository.save(doc);
        return new ShareResponseDTO(ShareMode.RESTRICTED.name(), null);
    }

    private ShareResponseDTO shareByLink(DocDocument doc, AuthUser owner, SharePermission permission) {
        DocumentShare link = documentShareRepository.findByDocumentId(doc.getId()).stream()
                .filter(s -> s.getShareMode() == ShareMode.ANYONE_WITH_LINK)
                .findFirst()
                .orElseGet(() -> DocumentShare.builder()
                        .documentId(doc.getId())
                        .sharedWithUserId(null)
                        .sharedByUserId(owner.getId())
                        .shareToken(UUID.randomUUID().toString().replace("-", ""))
                        .build());
        link.setPermission(permission);
        link.setShareMode(ShareMode.ANYONE_WITH_LINK);
        documentShareRepository.save(link);

        doc.setVisibility(DocumentVisibility.SHARED);
        docDocumentRepository.save(doc);
        return new ShareResponseDTO(ShareMode.ANYONE_WITH_LINK.name(), link.getShareToken());
    }

    private ShareResponseDTO disableLinkShare(Long documentId) {
        documentShareRepository.deleteByDocumentIdAndShareMode(documentId, ShareMode.ANYONE_WITH_LINK);
        return new ShareResponseDTO(ShareMode.NONE.name(), null);
    }

    @Override
    @Transactional
    public void revokeShare(Long documentId, Long userId) {
        AuthUser owner = getCurrentUser();
        requireOwnedDocument(documentId, owner);
        documentShareRepository.deleteByDocumentIdAndSharedWithUserId(documentId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SharedWithMeItemDTO> listSharedWithMe() {
        Long userId = getCurrentUser().getId();
        List<SharedWithMeItemDTO> result = new ArrayList<>();
        for (DocumentShare share : documentShareRepository.findBySharedWithUserId(userId)) {
            DocDocument doc = docDocumentRepository.findById(share.getDocumentId()).orElse(null);
            if (doc == null || doc.getDeletedAt() != null) continue; // ẩn tài liệu đã xóa
            DocumentResponseDTO docDto = documentMapper.toResponseDTO(doc);
            String sharedByName = authUserRepository.findById(share.getSharedByUserId())
                    .map(AuthUser::getFullName)
                    .orElse(null);
            result.add(new SharedWithMeItemDTO(docDto, share.getPermission().name(), sharedByName));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasShareAccess(Long documentId, Long userId) {
        if (userId == null) return false;
        if (documentShareRepository.existsByDocumentIdAndSharedWithUserId(documentId, userId)) {
            return true;
        }
        // V1: bật link-share (ANYONE_WITH_LINK) coi như mọi user đăng nhập tới được tài liệu đều
        // xem được. Gating chính xác theo token dành cho V2 (cần luồng resolve theo token riêng).
        return documentShareRepository.existsByDocumentIdAndShareMode(documentId, ShareMode.ANYONE_WITH_LINK);
    }
}
