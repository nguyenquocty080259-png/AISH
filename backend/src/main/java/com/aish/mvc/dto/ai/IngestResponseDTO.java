package com.aish.mvc.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IngestResponseDTO {

    private Long documentId;

    // INGESTED | NO_FILE | SKIPPED_NON_PDF | FILE_ERROR | EMPTY
    private String status;

    private int chunkCount;
    private String message;
}
