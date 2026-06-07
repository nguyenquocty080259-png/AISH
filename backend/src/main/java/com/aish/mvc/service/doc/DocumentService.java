package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.CommentDTO;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.*;
import com.aish.mvc.entity.enums.DocumentStatus;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.stor.*;
import com.aish.mvc.service.stor.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired private DocDocumentRepository docDocumentRepository;
    @Autowired private DocFileRepository docFileRepository;
    @Autowired private FileStorageService fileStorageService;

    @Autowired private CommentRepository commentRepository;
    @Autowired private FavoriteRepository favoriteRepository;
    @Autowired private RatingRepository ratingRepository;
    @Autowired private DownloadRepository downloadRepository;
    @Autowired private AuthUserRepository authUserRepository;

    private final Long CURRENT_USER_ID = 1L; // Giả lập User đang đăng nhập hệ thống

    public List<DocumentResponseDTO> getAllDocuments() {
        return docDocumentRepository.findByDeletedAtIsNull().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentResponseDTO uploadDocumentWithFile(String title, String description, MultipartFile file) {
        String storedFileName = fileStorageService.storeFile(file);
        DocDocument doc = new DocDocument();
        doc.setTitle(title);
        doc.setDescription(description);

        AuthUser user = authUserRepository.findById(CURRENT_USER_ID).orElse(new AuthUser());
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

    // LOGIC TƯƠNG TÁC 1: Thêm bình luận vào tài liệu
    @Transactional
    public void addComment(Long documentId, String content) {
        DocDocument doc = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));
        AuthUser user = authUserRepository.findById(CURRENT_USER_ID)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));

        Comment comment = Comment.builder()
                .document(doc)
                .user(user)
                .content(content)
                .build();
        commentRepository.save(comment);
    }

    // LOGIC TƯƠNG TÁC 2: Thả tim / Bỏ thích tài liệu (Toggle Favorite)
    @Transactional
    public void toggleFavorite(Long documentId) {
        // Sửa lỗi FavoriteId bằng cách check trực tiếp qua hàm Repo tùy biến để tránh lỗi biên dịch gói
        if (favoriteRepository.existsByUserIdAndDocumentId(CURRENT_USER_ID, documentId)) {
            favoriteRepository.deleteByUserIdAndDocumentId(CURRENT_USER_ID, documentId);
        } else {
            Favorite favorite = Favorite.builder()
                    .userId(CURRENT_USER_ID)
                    .documentId(documentId)
                    .createdAt(LocalDateTime.now())
                    .build();
            favoriteRepository.save(favorite);
        }
    }

    // LOGIC TƯƠNG TÁC 3: Đánh giá sao (Rating)
    @Transactional
    public void rateDocument(Long documentId, Integer star) {
        DocDocument doc = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));

        Rating rating = Rating.builder()
                .userId(CURRENT_USER_ID)
                .document(doc)
                .rating(star)
                .createdAt(LocalDateTime.now())
                .build();
        ratingRepository.save(rating);
    }

    // LOGIC TƯƠNG TÁC 4: Ghi nhận lịch sử khi Download thành công
    @Transactional
    public void logDownload(Long documentId) {
        DocDocument doc = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));

        Download download = Download.builder()
                .userId(CURRENT_USER_ID)
                .document(doc)
                .downloadedAt(LocalDateTime.now())
                .build();
        downloadRepository.save(download);
    }

    @Transactional
    public void deleteDocument(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));
        doc.setDeletedAt(LocalDateTime.now());
        docDocumentRepository.save(doc);
    }

    public List<DocumentResponseDTO> getDeletedDocuments() {
        return docDocumentRepository.findByDeletedAtIsNotNull().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void restoreDocument(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));
        doc.setDeletedAt(null);
        docDocumentRepository.save(doc);
    }

    public DocFile getFileByDocumentId(Long documentId) {
        DocDocument doc = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));
        return doc.getFiles().getFirst();
    }

    // Ánh xạ dữ liệu và tính toán thống kê tự động từ DB đưa lên DTO
    private DocumentResponseDTO mapToResponseDTO(DocDocument doc) {
        DocumentResponseDTO dto = new DocumentResponseDTO();
        dto.setId(doc.getId());
        dto.setTitle(doc.getTitle());
        dto.setDescription(doc.getDescription());
        dto.setStatus(doc.getStatus() != null ? doc.getStatus().name() : "COMPLETED");
        dto.setVisibility(doc.getVisibility() != null ? doc.getVisibility().name() : "PUBLIC");
        dto.setCreatedAt(doc.getCreatedAt());

        if (doc.getUser() != null) dto.setOwnerName(doc.getUser().getFullName());
        if (doc.getFiles() != null && !doc.getFiles().isEmpty()) dto.setFileName(doc.getFiles().getFirst().getFileName());

        // Đồng bộ các hàm thống kê đếm số lượng từ các Repo mới tạo
        dto.setFavoriteCount(favoriteRepository.countByDocumentId(doc.getId()));
        dto.setDownloadCount(downloadRepository.countByDocumentId(doc.getId()));
        dto.setAverageRating(ratingRepository.getAverageRatingByDocumentId(doc.getId()));
        dto.setFavorited(favoriteRepository.existsByUserIdAndDocumentId(CURRENT_USER_ID, doc.getId()));

        // Lấy danh sách bình luận đính kèm theo tài liệu
        List<CommentDTO> commentDTOs = commentRepository.findByDocumentIdOrderByCreatedAtDesc(doc.getId()).stream()
                .map(c -> new CommentDTO(c.getId(), c.getUser().getFullName(), c.getContent(), c.getCreatedAt()))
                .collect(Collectors.toList());
        dto.setComments(commentDTOs);

        return dto;
    }
}