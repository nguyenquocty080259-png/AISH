package com.aish.mvc.dto.report;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReportRequestDTO {

    private String targetType;
    private Long targetId;
    private String reason;
}
