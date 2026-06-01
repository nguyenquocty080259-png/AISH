package com.aish.mvc.service;

import com.aish.mvc.dto.DocumentRequestDTO;
import com.aish.mvc.dto.DocumentResponseDTO;
import com.aish.mvc.entity.AuthUser;
import com.aish.mvc.entity.DocDocument;
import com.aish.mvc.entity.enums.DocumentStatus;
import com.aish.mvc.entity.enums.Visibility;
import com.aish.mvc.repository.DocDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocDocumentRepository docDocumentRepository;

    
    /**
     *
     * @Autowired
    private DocFileRepository docFileRepository;
     * 
     */
    

    public List<DocumentResponseDTO> getAllDocuments() {
        // Sử dụng DocDocumentRepository mới
        return docDocumentRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());}

    @Transactional
    public DocDocument createDocument(DocumentRequestDTO request) {
        DocDocument doc = new DocDocument();
        doc.setTitle(request.getTitle());
        doc.setDescription(request.getDescription());
        
        // Ánh xạ User qua Object AuthUser thay vì chỉ dùng ID thuần
        AuthUser user = new AuthUser();
        user.setId(request.getUserId()); 
        doc.setUser(user); 
        
        // Sử dụng Enum chuẩn từ Entity bạn đã định nghĩa
        doc.setStatus(DocumentStatus.PROCESSING); 
        doc.setVisibility(Visibility.PRIVATE);

        return docDocumentRepository.save(doc);
    }

    private DocumentResponseDTO mapToResponseDTO(DocDocument doc) {
        DocumentResponseDTO dto = new DocumentResponseDTO();
        dto.setId(doc.getId());
        dto.setTitle(doc.getTitle());
        dto.setDescription(doc.getDescription());
        // Chuyển Enum sang String để FE dễ đọc
        dto.setVisibility(doc.getVisibility() != null ? doc.getVisibility().name() : "PRIVATE");
        // Giả sử BaseEntity của bạn dùng Instant hoặc LocalDateTime cho createdAt
        return dto;
    }
    @Transactional
public DocumentResponseDTO createDocumentAndReturnDTO(DocumentRequestDTO request) {
    // 1. Gọi hàm tạo Entity DocDocument (hàm bạn đã có)
    DocDocument savedDoc = createDocument(request); 
    
    // 2. Chuyển đổi Entity vừa lưu sang ResponseDTO để trả về
    return mapToResponseDTO(savedDoc); 
}// Lấy chi tiết 1 tài liệu theo ID và map sang ResponseDTO
    public DocumentResponseDTO getDocumentById(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu với ID: " + id));
        return mapToResponseDTO(doc);
    }

    // Xóa tài liệu theo ID
    @Transactional
    public void deleteDocument(Long id) {
        if (!docDocumentRepository.existsById(id)) {
            throw new RuntimeException("Tài liệu không tồn tại, không thể xóa!");
        }
        docDocumentRepository.deleteById(id);
    }
}