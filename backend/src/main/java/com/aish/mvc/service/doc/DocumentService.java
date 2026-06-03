package com.aish.mvc.service.doc;

import com.aish.mvc.dto.DocumentResponseDTO;
import com.aish.mvc.dto.DocumentRequestDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.entity.enums.DocumentStatus;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.repository.stor.DocDocumentRepository;
import com.aish.mvc.repository.stor.DocFileRepository;
import com.aish.mvc.service.stor.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocDocumentRepository docDocumentRepository;

    @Autowired
    private DocFileRepository docFileRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // 1. Lấy toàn bộ danh sách tài liệu
    public List<DocumentResponseDTO> getAllDocuments() {
        return docDocumentRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // 2. Xử lý Upload tài liệu kèm File thật
    @Transactional
    public DocumentResponseDTO uploadDocumentWithFile(String title, String description, MultipartFile file) {
        String storedFileName = fileStorageService.storeFile(file);

        DocDocument doc = new DocDocument();
        doc.setTitle(title);
        doc.setDescription(description);

        // Gán cứng User ID = 1
        AuthUser user = AuthUser.builder().id(1L).build();
        doc.setUser(user);

        doc.setStatus(DocumentStatus.COMPLETED);
        doc.setVisibility(DocumentVisibility.PUBLIC);

        DocDocument savedDoc = docDocumentRepository.save(doc);

        DocFile docFile = DocFile.builder()
                .fileName(file.getOriginalFilename())
                .fileUrl(storedFileName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .document(savedDoc)
                .build();
        docFileRepository.save(docFile);

        return mapToResponseDTO(savedDoc);
    }

    // 3. Lấy chi tiết 1 tài liệu
    public DocumentResponseDTO getDocumentById(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu với ID: " + id));
        return mapToResponseDTO(doc);
    }

    // 4. Xóa tài liệu
    @Transactional
    public void deleteDocument(Long id) {
        if (!docDocumentRepository.existsById(id)) {
            throw new RuntimeException("Tài liệu không tồn tại!");
        }
        docDocumentRepository.deleteById(id);
    }

    // 5. Lấy file để phục vụ Download (Đã sửa lỗi Static)
    public DocFile getFileByDocumentId(Long documentId) {
        // Sửa từ DocDocumentRepository thành docDocumentRepository (biến đã autowired)
        DocDocument doc = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));

        if (doc.getFiles() == null || doc.getFiles().isEmpty()) {
            throw new RuntimeException("Tài liệu này không có file đính kèm");
        }

        return doc.getFiles().get(0);
    }

    // Hàm phụ trợ: Chuyển đổi Entity sang DTO
    private DocumentResponseDTO mapToResponseDTO(DocDocument doc) {
        DocumentResponseDTO dto = new DocumentResponseDTO();
        dto.setId(doc.getId());
        dto.setTitle(doc.getTitle());
        dto.setDescription(doc.getDescription());
        dto.setStatus(doc.getStatus() != null ? doc.getStatus().name() : "PROCESSING");
        dto.setVisibility(doc.getVisibility() != null ? doc.getVisibility().name() : "PUBLIC");

        if (doc.getUser() != null) {
            dto.setOwnerName(doc.getUser().getFullName());
        }

        // Cập nhật: Lấy thêm tên file để React có cái hiển thị/tải về
        if (doc.getFiles() != null && !doc.getFiles().isEmpty()) {
            dto.setFileName(doc.getFiles().get(0).getFileName());
        }

        dto.setCreatedAt(doc.getCreatedAt());
        return dto;
    }

    @Transactional
    public DocumentResponseDTO createDocumentAndReturnDTO(DocumentRequestDTO request) {
        DocDocument doc = new DocDocument();
        doc.setTitle(request.getTitle());
        doc.setDescription(request.getDescription());
        doc.setUser(AuthUser.builder().id(1L).build());
        doc.setStatus(DocumentStatus.PROCESSING);
        doc.setVisibility(DocumentVisibility.PRIVATE);
        return mapToResponseDTO(docDocumentRepository.save(doc));
    }
}