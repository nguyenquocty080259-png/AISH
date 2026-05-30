package com.aish.mvc.service;

import com.aish.mvc.dto.DocumentRequestDTO;
import com.aish.mvc.dto.DocumentResponseDTO;
import com.aish.mvc.entity.Document;
import com.aish.mvc.entity.DocumentFile;
import com.aish.mvc.repository.DocumentRepository;
import com.aish.mvc.repository.DocumentFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentFileRepository documentFileRepository;

    public List<DocumentResponseDTO> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentResponseDTO createDocument(DocumentRequestDTO request) {
        // Lưu thông tin Document
        Document doc = new Document();
        doc.setUserId(request.getUserId());
        doc.setTitle(request.getTitle());
        doc.setDescription(request.getDescription());
        doc.setVisibility(request.getVisibility());
        Document savedDoc = documentRepository.save(doc);

        // Tạo bản ghi File trống để chờ Cloud upload
        DocumentFile file = new DocumentFile();
        file.setDocumentId(savedDoc.getId());
        file.setFileName(request.getFileName());
        file.setStorageUrl("PENDING_UPLOAD"); 
        documentFileRepository.save(file);

        return mapToResponseDTO(savedDoc);
    }

    private DocumentResponseDTO mapToResponseDTO(Document doc) {
        DocumentResponseDTO dto = new DocumentResponseDTO();
        dto.setId(doc.getId());
        dto.setTitle(doc.getTitle());
        dto.setDescription(doc.getDescription());
        dto.setVisibility(doc.getVisibility());
        dto.setCreatedAt(doc.getCreatedAt());
        return dto;
    }
}