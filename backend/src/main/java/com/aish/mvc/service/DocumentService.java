package com.aish.mvc.service;

import com.aish.mvc.dto.DocumentRequestDTO;
import com.aish.mvc.dto.DocumentResponseDTO;
import com.aish.mvc.entity.Document;
import com.aish.mvc.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    // Hàm lấy danh sách nhưng trả về DTO
    public List<DocumentResponseDTO> getAllDocuments() {
        List<Document> documents = documentRepository.findAll();
        
        // Chuyển đổi từ List<Document> sang List<DocumentResponseDTO>
        return documents.stream().map(doc -> {
            DocumentResponseDTO dto = new DocumentResponseDTO();
            dto.setId(doc.getId());
            dto.setTitle(doc.getTitle());
            dto.setDescription(doc.getDescription());
            dto.setFileName(doc.getFileName());
            dto.setStorageUrl(doc.getStorageUrl());
            dto.setVisibility(doc.getVisibility());
            dto.setCreatedAt(doc.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }
    public DocumentResponseDTO createDocument(DocumentRequestDTO request) {
        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setFileName(request.getFileName());
        document.setStorageUrl(request.getStorageUrl());
        document.setVisibility(request.getVisibility());
        document.setUserId(request.getUserId());
        
        Document savedDoc = documentRepository.save(document);
        
        DocumentResponseDTO response = new DocumentResponseDTO();
        response.setId(savedDoc.getId());
        response.setTitle(savedDoc.getTitle());
        response.setDescription(savedDoc.getDescription());
        response.setFileName(savedDoc.getFileName());
        response.setStorageUrl(savedDoc.getStorageUrl());
        response.setVisibility(savedDoc.getVisibility());
        response.setCreatedAt(savedDoc.getCreatedAt());
        
        return response;
    }
}