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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentShareServiceImpl implements DocumentShareService {

    private static final Logger log = LoggerFactory.getLogger(DocumentShareServiceImpl.class);

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

        // VALIDATE "PUBLIC": chỉ tài liệu ĐANG công khai (visibility == PUBLIC) mới được chia sẻ.
        // Ngữ nghĩa share mới: share KHÔNG đổi visibility, chỉ cấp thêm ĐẶC QUYỀN (hiện ở trang
        // "Được chia sẻ với tôi", quyền SharePermission, thông báo). Vì tài liệu vốn đã công khai,
        // share không cấp quyền xem. Do đó điều kiện đúng là gác thẳng theo visibility == PUBLIC:
        //   - Tài liệu PUBLIC đời cũ (moderationStatus = null) vẫn thỏa vì nhánh PUBLIC đã che.
        //   - Không còn nhánh SHARED/APPROVED: tài liệu PRIVATE dù đã duyệt cũng KHÔNG được share.
        // Tắt link-share (NONE) là thu hồi nên không cần gác.
        if (request.getMode() != ShareMode.NONE
                && doc.getVisibility() != DocumentVisibility.PUBLIC) {
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

            // B3: thông báo chạy transaction riêng (REQUIRES_NEW). Nếu lỗi (vd constraint DB drift)
            // thì CHỈ log warn — share vẫn thành công, không để notification làm rollback thao tác.
            try {
                notificationService.createDocumentNotification(
                        userId,
                        NotificationType.DOCUMENT_SHARED,
                        ownerName + " đã chia sẻ tài liệu \"" + doc.getTitle() + "\" với bạn.",
                        doc.getId());
            } catch (Exception ex) {
                log.warn("Không tạo được thông báo DOCUMENT_SHARED cho user {} (tài liệu {}): {}",
                        userId, doc.getId(), ex.getMessage());
            }
        }

        // B1: share KHÔNG đổi visibility. Tài liệu giữ nguyên PUBLIC — chỉ cấp thêm đặc quyền qua
        // bảng document_shares. Gỡ chia sẻ chỉ rút đặc quyền; người bị gỡ vẫn xem được vì PUBLIC.
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
    // B5: số điện thoại nằm ở auth_user_profiles chứ KHÔNG phải auth_accounts.identifier (identifier
    // chỉ chứa email của LOCAL/GOOGLE/GITHUB), nhưng vẫn phòng thủ bằng cách chỉ nhận bản ghi có
    // dạng email (chứa '@'). Nếu email khớp NHIỀU tài khoản khác nhau -> KHÔNG chọn ngầm, từ chối và
    // ghi log để tránh chia sẻ nhầm tài khoản.
    private AuthUser resolveUserByEmail(String rawEmail) {
        String email = rawEmail.trim();
        List<AuthAccount> matches = authAccountRepository.findByIdentifierIgnoreCase(email).stream()
                .filter(a -> a.getUser() != null)
                .filter(a -> a.getIdentifier() != null && a.getIdentifier().contains("@"))
                .toList();
        if (matches.isEmpty()) {
            throw new ResourceNotFoundException("Email này chưa có tài khoản HiveMind.");
        }
        List<Long> distinctUserIds = matches.stream()
                .map(a -> a.getUser().getId())
                .distinct()
                .toList();
        if (distinctUserIds.size() > 1) {
            log.warn("Email {} khớp nhiều tài khoản HiveMind khác nhau (userIds={}); từ chối chia sẻ để tránh chọn nhầm.",
                    email, distinctUserIds);
            throw new IllegalStateException(
                    "Email này khớp nhiều tài khoản. Vui lòng liên hệ quản trị viên để xử lý.");
        }
        return matches.get(0).getUser();
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

        // B1: KHÔNG đổi visibility. Tài liệu vốn đã PUBLIC (đã qua gate) nên giữ nguyên.
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
