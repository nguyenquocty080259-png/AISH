package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.ModerationKeywordCreateRequestDTO;
import com.aish.mvc.dto.doc.ModerationKeywordResponseDTO;
import com.aish.mvc.dto.doc.ModerationKeywordUpdateRequestDTO;
import com.aish.mvc.entity.enums.ModerationKeywordType;

import java.util.List;

public interface ModerationKeywordService {

    List<ModerationKeywordResponseDTO> list(ModerationKeywordType type);

    ModerationKeywordResponseDTO create(ModerationKeywordCreateRequestDTO request);

    ModerationKeywordResponseDTO update(Long id, ModerationKeywordUpdateRequestDTO request);

    void delete(Long id);
}
