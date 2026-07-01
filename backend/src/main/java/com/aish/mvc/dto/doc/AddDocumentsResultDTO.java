package com.aish.mvc.dto.doc;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// Kết quả POST .../documents: phân loại từng documentId gửi lên.
@Getter
@Setter
public class AddDocumentsResultDTO {
    private List<Long> added = new ArrayList<>();
    private List<Long> alreadyPresent = new ArrayList<>();
    private List<Long> skippedNoPermission = new ArrayList<>();
}
