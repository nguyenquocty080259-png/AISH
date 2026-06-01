package com.aish.mvc.controller;
import com.aish.mvc.dto.DocumentRequestDTO;
import com.aish.mvc.dto.DocumentResponseDTO;
import com.aish.mvc.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:5173")

@RestController
@RequestMapping("/api/documents")

public class DocumentController {

    @Autowired
    private DocumentService documentService;

    // Lấy toàn bộ danh sách tài liệu
    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> getAll() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    // Tạo tài liệu mới
    @PostMapping
    public ResponseEntity<DocumentResponseDTO> create(@RequestBody DocumentRequestDTO request) {
        // Gọi service tạo xong, chúng ta map nó về ResponseDTO luôn cho đồng bộ
        return ResponseEntity.ok(documentService.createDocumentAndReturnDTO(request));
    }
    // Xem chi tiết 1 tài liệu (GET /api/documents/{id})
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    // Xóa 1 tài liệu (DELETE /api/documents/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok("Đã xóa tài liệu có ID: " + id + " thành công!");
    }
}