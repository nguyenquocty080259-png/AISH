package com.aish.mvc.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CitationDTO {

    private Long documentId;
    private String title;
    private String author;
    // null cho định dạng không có trang thật (TXT/DOCX...) — snippet vẫn đủ để định vị/highlight.
    private Integer page;
    private String snippet;
}
