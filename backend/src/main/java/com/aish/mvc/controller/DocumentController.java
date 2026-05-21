package com.aish.mvc.controller;

import com.aish.mvc.dto.DocumentResponseDTO;
import com.aish.mvc.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> getAll() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }
}