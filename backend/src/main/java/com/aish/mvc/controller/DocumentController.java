package com.aish.mvc.controller;

import com.aish.mvc.dto.DocumentRequestDTO;
import com.aish.mvc.dto.DocumentResponseDTO;
import com.aish.mvc.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> getAll() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }
    @PostMapping
    public ResponseEntity<DocumentResponseDTO> create(@RequestBody DocumentRequestDTO request) {
        DocumentResponseDTO response = documentService.createDocument(request);
        return ResponseEntity.ok(response);
    }
}