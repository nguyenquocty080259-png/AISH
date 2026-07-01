package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.ModerationAppealResponseDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.ModerationAppeal;
import com.aish.mvc.entity.enums.AppealStatus;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.exception.ForbiddenException;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.ModerationAppealRepository;
import com.aish.mvc.service.doc.ModerationAppealService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModerationAppealServiceImpl implements ModerationAppealService {

    private final DocDocumentRepository docDocumentRepository;
    private final ModerationAppealRepository moderationAppealRepository;
    private final AuthAccountRepository authAccountRepository;

    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    @Override
    @Transactional
    public ModerationAppealResponseDTO appeal(Long documentId, String reason) {
        DocDocument doc = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));

        AuthUser currentUser = getCurrentUser();
        if (!doc.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Bạn không có quyền kháng cáo tài liệu này!");
        }

        if (doc.getModerationStatus() != ModerationStatus.REJECTED) {
            throw new IllegalStateException("Chỉ có thể kháng cáo tài liệu đã bị từ chối (REJECTED).");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Lý do kháng cáo không được để trống.");
        }

        // Guard chống trùng: chặn ngay lúc TẠO thay vì lọc lại ở hàng chờ Admin — 1 tài liệu
        // chỉ có tối đa 1 appeal đang PENDING, tránh nhiều dòng trùng nhau trong queue.
        if (moderationAppealRepository.existsByDocument_IdAndStatus(documentId, AppealStatus.APPEAL_PENDING)) {
            throw new IllegalStateException("Tài liệu này đã có một kháng cáo đang chờ Admin xử lý.");
        }

        ModerationAppeal appeal = ModerationAppeal.builder()
                .document(doc)
                .user(currentUser)
                .reason(reason.strip())
                .status(AppealStatus.APPEAL_PENDING)
                .build();

        moderationAppealRepository.save(appeal);

        return toDTO(appeal);
    }

    private ModerationAppealResponseDTO toDTO(ModerationAppeal appeal) {
        return new ModerationAppealResponseDTO(
                appeal.getId(),
                appeal.getDocument().getId(),
                appeal.getReason(),
                appeal.getStatus(),
                appeal.getCreatedAt());
    }
}
