package com.aish.mvc.controller.doc;

import com.aish.mvc.dto.DocumentRequestDTO;
import com.aish.mvc.dto.DocumentResponseDTO;
import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.service.doc.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173") // Cổng mặc định của Vite React
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    // Lấy đường dẫn thư mục upload từ file application.properties
    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * 1. Lấy danh sách toàn bộ tài liệu
     */
    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> getAll() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    /**
     * 2. Xem chi tiết thông tin một tài liệu
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    /**
     * 3. Upload tài liệu mới kèm file vật lý
     */
    @PostMapping("/upload")
    public ResponseEntity<DocumentResponseDTO> upload(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file) {

        DocumentResponseDTO result = documentService.uploadDocumentWithFile(title, description, file);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    /**
     * 4. Xóa tài liệu (Xóa cứng)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 5. Tải xuống tài liệu (Download)
     * Hàm này sẽ tìm file thực tế dựa trên Document ID
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        DocFile docFile = documentService.getFileByDocumentId(id);
        try {
            // Thêm Log để mình nhìn tận mắt nó tìm ở đâu
            Path filePath = Paths.get(uploadDir).resolve(docFile.getFileUrl()).normalize();
            System.out.println("DEBUG: Looking for file at " + filePath.toAbsolutePath());

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + docFile.getFileName() + "\"")
                        .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION) // Cho phép React đọc header này
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}