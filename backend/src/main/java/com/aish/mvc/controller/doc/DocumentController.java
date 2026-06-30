package com.aish.mvc.controller.doc;

import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.service.doc.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @org.springframework.beans.factory.annotation.Value("${app.upload.dir}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> getAll() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    // LUỒNG 1: lên server (ổ đĩa)
    @PostMapping("/upload-server")
    public ResponseEntity<DocumentResponseDTO> uploadServer(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "subjectIds", required = false) java.util.List<Long> subjectIds,
            @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(
                documentService.uploadDocumentToServer(title, description, subjectIds, file),
                HttpStatus.CREATED);
    }

    // LUỒNG 2: lên cloud (Cloudinary)
    @PostMapping("/upload-cloud")
    public ResponseEntity<DocumentResponseDTO> uploadCloud(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "subjectIds", required = false) java.util.List<Long> subjectIds,
            @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(
                documentService.uploadDocumentToCloud(title, description, subjectIds, file),
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

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentDelete(@PathVariable Long id) {
        documentService.permanentDeleteDocument(id);
        return ResponseEntity.noContent().build();
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
        DocFile docFile = documentService.getFileByDocumentId(id);
        try {
            documentService.logDownload(id);
            String fileUrl = docFile.getFileUrl();
            Resource resource;
            if (fileUrl != null && fileUrl.startsWith("http")) {
                resource = new UrlResource(new java.net.URL(fileUrl));
            } else {
                Path filePath = Paths.get(uploadDir).resolve(fileUrl).normalize();
                resource = new UrlResource(filePath.toUri());
            }
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + docFile.getFileName() + "\"")
                        .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}