package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.AddDocumentsResultDTO;
import com.aish.mvc.dto.doc.CollectionDetailResponseDTO;
import com.aish.mvc.dto.doc.CollectionResponseDTO;

import java.util.List;

public interface CollectionService {

    CollectionResponseDTO createCollection(String name);

    List<CollectionResponseDTO> getMyCollections();

    CollectionDetailResponseDTO getCollectionDetail(Long id);

    CollectionResponseDTO renameCollection(Long id, String name);

    void deleteCollection(Long id);

    AddDocumentsResultDTO addDocuments(Long id, List<Long> documentIds);

    void removeDocument(Long id, Long documentId);
}
