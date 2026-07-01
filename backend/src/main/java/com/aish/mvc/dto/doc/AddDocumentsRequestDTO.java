package com.aish.mvc.dto.doc;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

// Body cho add nhiều doc: { "documentIds": [1, 2, 3] }
@Getter
@Setter
public class AddDocumentsRequestDTO {
    private List<Long> documentIds;
}
