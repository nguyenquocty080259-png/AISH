package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.ModerationKeywordCreateRequestDTO;
import com.aish.mvc.dto.doc.ModerationKeywordResponseDTO;
import com.aish.mvc.dto.doc.ModerationKeywordUpdateRequestDTO;
import com.aish.mvc.entity.doc.ModerationKeyword;
import com.aish.mvc.entity.enums.ModerationKeywordType;
import com.aish.mvc.repository.doc.ModerationKeywordRepository;
import com.aish.mvc.service.doc.ModerationKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

/**
 * QUẢN LÝ DANH SÁCH TỪ KHOÁ CẤM cho trang quản trị (thêm/sửa/xoá/liệt kê).
 *
 * <p>Từ khoá được chia theo loại dùng: NAMING (đặt tên tài liệu, bộ sưu tập), COMMENT (bình
 * luận), DOCUMENT_CONTENT (nội dung tài liệu khi xin công khai). Vì lưu trong database nên Admin
 * sửa là có hiệu lực ngay, không cần lập trình viên sửa code hay khởi động lại server.
 *
 * <p>Mọi từ khoá được chuẩn hoá về CHỮ THƯỜNG trước khi lưu, và không cho trùng trong cùng một loại.
 */
@Service
@RequiredArgsConstructor
public class ModerationKeywordServiceImpl implements ModerationKeywordService {

    private final ModerationKeywordRepository moderationKeywordRepository;

    /**
     * Liệt kê từ khoá, mới nhất trước. Đầu vào: loại từ khoá muốn lọc, để null thì lấy tất cả.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ModerationKeywordResponseDTO> list(ModerationKeywordType type) {
        List<ModerationKeyword> keywords = type == null
                ? moderationKeywordRepository.findAllByOrderByCreatedAtDesc()
                : moderationKeywordRepository.findByTypeOrderByCreatedAtDesc(type);
        return keywords.stream().map(this::toResponse).toList();
    }

    /**
     * THÊM một từ khoá cấm. Đầu vào: từ khoá + loại. Trả về: từ khoá vừa tạo (mặc định đang bật).
     * Các bước: (1) chuẩn hoá chữ thường + chặn chuỗi rỗng, (2) chặn trùng, (3) lưu xuống DB.
     */
    @Override
    @Transactional
    public ModerationKeywordResponseDTO create(ModerationKeywordCreateRequestDTO request) {
        String keyword = normalizeAndValidate(request.getKeyword());
        // Cùng một từ trong cùng một loại thì không thêm lần hai.
        if (moderationKeywordRepository.existsByKeywordAndType(keyword, request.getType())) {
            throw duplicateKeyword();
        }

        ModerationKeyword entity = ModerationKeyword.builder()
                .keyword(keyword)
                .type(request.getType())
                .active(true)
                .build();
        return toResponse(save(entity));
    }

    /**
     * SỬA từ khoá: đổi chữ và/hoặc bật-tắt. Đầu vào: id + các trường muốn đổi (null = giữ nguyên).
     * Tắt (active = false) thì từ khoá còn trong danh sách nhưng thôi không dùng để chặn nữa —
     * tiện hơn xoá hẳn khi Admin chỉ muốn tạm ngưng một quy tắc.
     */
    @Override
    @Transactional
    public ModerationKeywordResponseDTO update(Long id, ModerationKeywordUpdateRequestDTO request) {
        ModerationKeyword entity = moderationKeywordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy từ khóa kiểm duyệt."));

        if (request.getKeyword() == null && request.getActive() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.keyword.updateContentRequired");
        }

        if (request.getKeyword() != null) {
            String keyword = normalizeAndValidate(request.getKeyword());
            if (moderationKeywordRepository.existsByKeywordAndTypeAndIdNot(keyword, entity.getType(), id)) {
                throw duplicateKeyword();
            }
            entity.setKeyword(keyword);
        }
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }

        return toResponse(save(entity));
    }

    /** XOÁ hẳn một từ khoá khỏi danh sách. Đầu vào: id. Không tìm thấy thì trả lỗi 404. */
    @Override
    @Transactional
    public void delete(Long id) {
        ModerationKeyword entity = moderationKeywordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy từ khóa kiểm duyệt."));
        moderationKeywordRepository.delete(entity);
    }

    private ModerationKeyword save(ModerationKeyword keyword) {
        try {
            return moderationKeywordRepository.saveAndFlush(keyword);
        } catch (DataIntegrityViolationException exception) {
            throw duplicateKeyword();
        }
    }

    private String normalizeAndValidate(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error.keyword.blank");
        }
        return normalized;
    }

    private ResponseStatusException duplicateKeyword() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT, "Từ khóa đã tồn tại trong loại kiểm duyệt này.");
    }

    private ModerationKeywordResponseDTO toResponse(ModerationKeyword keyword) {
        return new ModerationKeywordResponseDTO(
                keyword.getId(),
                keyword.getKeyword(),
                keyword.getType(),
                keyword.isActive(),
                keyword.getCreatedAt()
        );
    }
}
