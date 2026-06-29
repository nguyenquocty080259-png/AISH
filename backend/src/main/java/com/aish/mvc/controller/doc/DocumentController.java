package com.aish.mvc.controller.doc;

import com.aish.mvc.dto.doc.DocumentDownloadResult;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.service.doc.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentController {

    @Autowired private DocumentService documentService;

    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> getAll() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    // LUỒNG 1: lên server (ổ đĩa)
    @PostMapping("/upload-server")
    public ResponseEntity<DocumentResponseDTO> uploadServer(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "tags", required = false) java.util.List<String> tags,
            @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(
                documentService.uploadDocumentToServer(title, description, subjectId, tags, file),
                HttpStatus.CREATED);
    }

    // LUỒNG 2: lên cloud (Cloudinary)
    @PostMapping("/upload-cloud")
    public ResponseEntity<DocumentResponseDTO> uploadCloud(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "tags", required = false) java.util.List<String> tags,
            @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(
                documentService.uploadDocumentToCloud(title, description, subjectId, tags, file),
                HttpStatus.CREATED);
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Void> toggleFavorite(@PathVariable Long id) {
        documentService.toggleFavorite(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Void> addComment(@PathVariable Long id, @RequestBody String content) {
        documentService.addComment(id, content);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<Void> rate(@PathVariable Long id, @RequestParam Integer star) {
        documentService.rateDocument(id, star);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trash")
    public ResponseEntity<List<DocumentResponseDTO>> getTrash() {
        return ResponseEntity.ok(documentService.getDeletedDocuments());
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        documentService.restoreDocument(id);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{id}/toggle-visibility")
    public ResponseEntity<Void> toggleVisibility(@PathVariable Long id) {
        documentService.toggleVisibility(id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            DocumentDownloadResult result = documentService.prepareDownload(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                    .body(result.resource());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}