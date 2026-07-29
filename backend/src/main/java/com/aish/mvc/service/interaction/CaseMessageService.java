package com.aish.mvc.service.interaction;

import com.aish.mvc.dto.interaction.CaseMessageDTO;
import com.aish.mvc.entity.enums.CaseType;

import java.util.List;

public interface CaseMessageService {

    List<CaseMessageDTO> listMessages(CaseType caseType, Long caseId);

    CaseMessageDTO postMessage(CaseType caseType, Long caseId, String content);
}
