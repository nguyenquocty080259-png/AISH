package com.aish.mvc.controller.doc;

import com.aish.mvc.dto.doc.AddDocumentsRequestDTO;
import com.aish.mvc.dto.doc.AddDocumentsResultDTO;
import com.aish.mvc.dto.doc.CollectionDetailResponseDTO;
import com.aish.mvc.dto.doc.CollectionRequestDTO;
import com.aish.mvc.dto.doc.CollectionResponseDTO;
import com.aish.mvc.service.doc.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    // Tạo collection { name }
    @PostMapping
    public ResponseEntity<CollectionResponseDTO> create(@RequestBody CollectionRequestDTO request) {
        return new ResponseEntity<>(collectionService.createCollection(request.getName()), HttpStatus.CREATED);
    }

    // List collection của user + đếm số doc mỗi collection
    @GetMapping
    public ResponseEntity<List<CollectionResponseDTO>> getMine() {
        return ResponseEntity.ok(collectionService.getMyCollections());
    }

    // Chi tiết + item (kèm availability)
    @GetMapping("/{id}")
    public ResponseEntity<CollectionDetailResponseDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(collectionService.getCollectionDetail(id));
    }

    // Đổi tên { name }
    @PutMapping("/{id}")
    public ResponseEntity<CollectionResponseDTO> rename(@PathVariable Long id, @RequestBody CollectionRequestDTO request) {
        return ResponseEntity.ok(collectionService.renameCollection(id, request.getName()));
    }

    // Xóa collection — KHÔNG xóa document
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.noContent().build();
    }

    // Add nhiều doc { documentIds: [...] }
    @PostMapping("/{id}/documents")
    public ResponseEntity<AddDocumentsResultDTO> addDocuments(@PathVariable Long id, @RequestBody AddDocumentsRequestDTO request) {
        return ResponseEntity.ok(collectionService.addDocuments(id, request.getDocumentIds()));
    }

    // Gỡ 1 doc khỏi collection
    @DeleteMapping("/{id}/documents/{docId}")
    public ResponseEntity<Void> removeDocument(@PathVariable Long id, @PathVariable Long docId) {
        collectionService.removeDocument(id, docId);
        return ResponseEntity.noContent().build();
    }
}
