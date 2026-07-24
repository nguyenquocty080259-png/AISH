package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.dto.doc.DocumentShareRecipientDTO;
import com.aish.mvc.dto.doc.ShareRequestDTO;
import com.aish.mvc.dto.doc.ShareResponseDTO;
import com.aish.mvc.dto.doc.SharedWithMeItemDTO;
import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.DocumentShare;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.entity.enums.NotificationType;
import com.aish.mvc.entity.enums.SharePermission;
import com.aish.mvc.entity.enums.ShareMode;
import com.aish.mvc.entity.enums.UserStatus;
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
import java.util.Comparator;
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

        // VALIDATE "PUBLIC": chỉ tài liệu CÔNG KHAI mới được chia sẻ — chặn chia sẻ tài liệu
        // riêng tư/chưa kiểm duyệt. "Công khai" ở đây KHÔNG thể gác cứng theo visibility == PUBLIC
        // vì hai lý do trong dữ liệu thật:
        //   (1) Chia sẻ thành công lại set visibility = SHARED (để gate truy cập theo từng người),
        //       nên lần chia sẻ thứ hai / re-share sẽ tự mâu thuẫn nếu chỉ chấp nhận PUBLIC.
        //   (2) Tài liệu public "đời cũ" có visibility = PUBLIC nhưng moderationStatus = null
        //       (chưa đi qua luồng kiểm duyệt DEC-035), nên gác cứng theo APPROVED cũng chặn nhầm.
        // Vì vậy: cho chia sẻ nếu tài liệu ĐANG lộ ra ngoài chủ sở hữu (PUBLIC hoặc SHARED) HOẶC
        // đã từng được duyệt công khai (moderationStatus APPROVED). Chỉ chặn tài liệu PRIVATE chưa
        // từng được duyệt. Tắt link-share (NONE) là thu hồi nên không cần gác.
        boolean shareable = doc.getVisibility() == DocumentVisibility.PUBLIC
                || doc.getVisibility() == DocumentVisibility.SHARED
                || doc.getModerationStatus() == ModerationStatus.APPROVED;
        if (request.getMode() != ShareMode.NONE && !shareable) {
            throw new IllegalArgumentException(
                    "Chỉ tài liệu công khai mới có thể chia sẻ. Hãy chuyển tài liệu sang công khai trước.");
        }

        return switch (request.getMode()) {
            case RESTRICTED -> shareToUsers(doc, owner, request, permission);
            case ANYONE_WITH_LINK -> shareByLink(doc, owner, permission);
            case NONE -> disableLinkShare(documentId);
        };
    }

    private ShareResponseDTO shareToUsers(
            DocDocument doc, AuthUser owner, ShareRequestDTO request, SharePermission permission) {
        List<Long> userIds = resolveTargetUserIds(request, owner);
        String ownerName = owner.getFullName();
        for (Long userId : userIds) {
            DocumentShare share = documentShareRepository
                    .findByDocumentIdAndSharedWithUserId(doc.getId(), userId)
                    .orElseGet(() -> DocumentShare.builder()
                            .documentId(doc.getId())
                            .sharedWithUserId(userId)
                            .sharedByUserId(owner.getId())
                            .build());
            // Upsert: người đã được chia sẻ + đổi quyền -> cập nhật row cũ, KHÔNG tạo row trùng.
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

    // Từ request -> danh sách userId hợp lệ để chia sẻ. Ưu tiên EMAIL (luồng modal V1); nếu không
    // có email thì rơi về userIds (tương thích ngược). Chặn tự chia sẻ cho chính mình và tài khoản
    // bị khóa, báo lỗi rõ ràng thay vì âm thầm bỏ qua.
    private List<Long> resolveTargetUserIds(ShareRequestDTO request, AuthUser owner) {
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            AuthUser target = resolveUserByEmail(request.getEmail());
            validateShareTarget(target, owner);
            return List.of(target.getId());
        }
        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            List<Long> ids = new ArrayList<>();
            for (Long userId : request.getUserIds()) {
                if (userId == null) continue;
                AuthUser target = authUserRepository.findById(userId).orElse(null);
                if (target == null) continue;
                validateShareTarget(target, owner);
                if (!ids.contains(userId)) ids.add(userId);
            }
            if (ids.isEmpty()) {
                throw new IllegalArgumentException("Cần chọn ít nhất một người để chia sẻ.");
            }
            return ids;
        }
        throw new IllegalArgumentException("Cần nhập email người nhận để chia sẻ.");
    }

    // Resolve email -> user đã có tài khoản HiveMind. Trim + so khớp không phân biệt hoa/thường.
    // Email là DUY NHẤT ở tầng ứng dụng (đăng ký local/OAuth đều chặn email trùng), nên gần như
    // luôn có 0 hoặc 1 kết quả; nếu vì lệch hoa/thường mà có nhiều bản ghi thì ưu tiên tài khoản
    // ACTIVE + là tài khoản chính để không chọn nhầm bản ghi phụ.
    private AuthUser resolveUserByEmail(String rawEmail) {
        String email = rawEmail.trim();
        List<AuthAccount> accounts = authAccountRepository.findByIdentifierIgnoreCase(email);
        return accounts.stream()
                .filter(a -> a.getUser() != null)
                .min(Comparator
                        .comparing((AuthAccount a) -> a.getUser().getStatus() == UserStatus.ACTIVE ? 0 : 1)
                        .thenComparing(a -> Boolean.TRUE.equals(a.getIsPrimary()) ? 0 : 1))
                .map(AuthAccount::getUser)
                .orElseThrow(() -> new ResourceNotFoundException("Email này chưa có tài khoản HiveMind."));
    }

    private void validateShareTarget(AuthUser target, AuthUser owner) {
        if (target.getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Bạn không thể tự chia sẻ tài liệu cho chính mình.");
        }
        if (target.getStatus() == UserStatus.BANNED) {
            throw new IllegalArgumentException("Tài khoản này đã bị khóa, không thể chia sẻ.");
        }
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
    public List<DocumentShareRecipientDTO> listShareRecipients(Long documentId) {
        // CHỈ chủ sở hữu được xem danh sách này (người được chia sẻ KHÔNG thấy ai khác).
        AuthUser owner = getCurrentUser();
        requireOwnedDocument(documentId, owner);

        List<DocumentShareRecipientDTO> result = new ArrayList<>();
        for (DocumentShare share : documentShareRepository.findByDocumentId(documentId)) {
            // Bỏ qua dòng link-share (ANYONE_WITH_LINK) — không gắn với 1 người cụ thể.
            if (share.getSharedWithUserId() == null) continue;
            AuthUser target = authUserRepository.findById(share.getSharedWithUserId()).orElse(null);
            if (target == null) continue;
            String email = authAccountRepository
                    .findFirstByUser_IdAndIsPrimaryTrue(target.getId())
                    .map(AuthAccount::getIdentifier)
                    .orElse(null);
            result.add(new DocumentShareRecipientDTO(
                    target.getId(),
                    target.getFullName(),
                    email,
                    share.getPermission().name()));
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
