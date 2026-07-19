package com.aish.mvc.dto.doc;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminStatsDTO {
    private long totalUsers;
    private long totalDocuments;
    private long publicDocuments;
    private long privateDocuments;
    private long pendingAppeals;
    private long totalSubjects;
    private long docsIngested;
    private long docsNotIngested;
    private long docsUnsupported;
    private long usedLocalBytes;
    private long usedCloudBytes;
}
