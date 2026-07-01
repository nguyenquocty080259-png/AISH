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

    // Trang Cộng đồng: chỉ tài liệu công khai (PUBLIC), có tìm kiếm + lọc theo môn + sắp xếp + phân trang
    // sortBy: "newest" (mặc định) | "downloads" | "rating"
    @GetMapping("/community")
    public ResponseEntity<com.aish.mvc.dto.doc.CommunityPageResponseDTO> getCommunity(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "sortBy", required = false, defaultValue = "newest") String sortBy,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "12") int size) {
        return ResponseEntity.ok(documentService.getCommunityDocuments(keyword, subjectId, sortBy, page, size));
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
            Resource resource = resolveResource(docFile);
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

    // Xem trước tài liệu (kiểu Studocu): trả file "inline" để FE nhúng iframe/pdf.js,
    // KHÔNG tính là lượt tải và không ép tải xuống.
    // Cho phép nếu: tài liệu PUBLIC, hoặc PRIVATE nhưng người xem là chủ sở hữu.
    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewFile(@PathVariable Long id) {
        try {
            DocFile docFile = documentService.getFileForPreview(id);
            Resource resource = resolveResource(docFile);
            if (resource.exists() || resource.isReadable()) {
                String contentType = docFile.getFileType() != null ? docFile.getFileType() : "application/octet-stream";
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + docFile.getFileName() + "\"")
                        .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private Resource resolveResource(DocFile docFile) throws java.net.MalformedURLException {
        String fileUrl = docFile.getFileUrl();
        if (fileUrl != null && fileUrl.startsWith("http")) {
            return new UrlResource(new java.net.URL(fileUrl));
        }
        Path filePath = Paths.get(uploadDir).resolve(fileUrl).normalize();
        return new UrlResource(filePath.toUri());
    }
}