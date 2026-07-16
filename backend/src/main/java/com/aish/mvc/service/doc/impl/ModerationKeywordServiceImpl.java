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

@Service
@RequiredArgsConstructor
public class ModerationKeywordServiceImpl implements ModerationKeywordService {

    private final ModerationKeywordRepository moderationKeywordRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ModerationKeywordResponseDTO> list(ModerationKeywordType type) {
        List<ModerationKeyword> keywords = type == null
                ? moderationKeywordRepository.findAllByOrderByCreatedAtDesc()
                : moderationKeywordRepository.findByTypeOrderByCreatedAtDesc(type);
        return keywords.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ModerationKeywordResponseDTO create(ModerationKeywordCreateRequestDTO request) {
        String keyword = normalizeAndValidate(request.getKeyword());
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

    @Override
    @Transactional
    public ModerationKeywordResponseDTO update(Long id, ModerationKeywordUpdateRequestDTO request) {
        ModerationKeyword entity = moderationKeywordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy từ khóa kiểm duyệt."));

        if (request.getKeyword() == null && request.getActive() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cần cung cấp nội dung cần cập nhật.");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Từ khóa không được để trống.");
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
