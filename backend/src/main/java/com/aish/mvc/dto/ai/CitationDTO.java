package com.aish.mvc.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CitationDTO {

    private Long documentId;
    private String documentTitle;
    private Integer page;
    private String snippet;
}
