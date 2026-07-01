package com.aish.mvc.dto.doc;

import lombok.Getter;
import lombok.Setter;

// Body tuỳ chọn cho POST /api/admin/appeals/{id}/approve|reject.
@Getter
@Setter
public class AppealDecisionRequestDTO {
    private String note;
}
