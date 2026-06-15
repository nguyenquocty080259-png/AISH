package com.aish.mvc.service.doc;

import com.aish.mvc.dto.doc.CommentDTO;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.*;
import com.aish.mvc.entity.enums.DocumentStatus;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.stor.*;
import com.aish.mvc.service.stor.CloudinaryService;
import com.aish.mvc.service.stor.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.aish.mvc.entity.doc.Subject;
import com.aish.mvc.repository.stor.SubjectRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.aish.mvc.entity.doc.Tag;
import com.aish.mvc.repository.stor.TagRepository;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

@Service
public class DocumentService {

    @Autowired private DocDocumentRepository docDocumentRepository;
    @Autowired private DocFileRepository docFileRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private TagRepository tagRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private FavoriteRepository favoriteRepository;
    @Autowired private RatingRepository ratingRepository;
    @Autowired private DownloadRepository downloadRepository;
    @Autowired private AuthUserRepository authUserRepository;
    @Autowired private AuthAccountRepository authAccountRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private CloudinaryService cloudinaryService;

    // Lấy user đang đăng nhập từ token (JwtAuthFilter đã set email làm principal)
    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> getAllDocuments() {
        Long uid = getCurrentUser().getId();
        return docDocumentRepository.findVisibleDocuments(DocumentVisibility.PUBLIC, uid).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentResponseDTO uploadDocumentWithFile(String title, String description, Long subjectId,
                                                      List<String> tagNames, MultipartFile file) {
        Map<String, String> uploaded = cloudinaryService.upload(file);
        DocDocument doc = new DocDocument();
        doc.setTitle(title);
        doc.setDescription(description);
        doc.setUser(getCurrentUser());
        doc.setStatus(DocumentStatus.COMPLETED);
        doc.setVisibility(DocumentVisibility.PUBLIC);

        if (subjectId != null) {
            Subject subject = subjectRepository.findById(subjectId).orElse(null);
            doc.setSubject(subject);
        }

        // Xử lý tags: tag nào chưa có thì tạo mới
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (String name : tagNames) {
                if (name == null || name.trim().isEmpty()) continue;
                String clean = name.trim();
                Tag tag = tagRepository.findByName(clean)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(clean).build()));
                tags.add(tag);
            }
            doc.setTags(tags);
        }

        DocDocument savedDoc = docDocumentRepository.save(doc);

        DocFile docFile = DocFile.builder()
                .fileName(file.getOriginalFilename())
                .fileUrl(uploaded.get("url"))
                .publicId(uploaded.get("publicId"))
                .resourceType(uploaded.get("resourceType"))
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

        Comment comment = Comment.builder()
                .document(doc)
                .user(getCurrentUser())
                .content(content)
                .build();
        commentRepository.save(comment);
    }

    // LOGIC TƯƠNG TÁC 2: Thả tim / Bỏ thích tài liệu (Toggle Favorite)
    @Transactional
    public void toggleFavorite(Long documentId) {
        Long uid = getCurrentUser().getId();
        if (favoriteRepository.existsByUserIdAndDocumentId(uid, documentId)) {
            favoriteRepository.deleteByUserIdAndDocumentId(uid, documentId);
        } else {
            Favorite favorite = Favorite.builder()
                    .userId(uid)
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
                .userId(getCurrentUser().getId())
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
                .userId(getCurrentUser().getId())
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

    @Transactional(readOnly = true)
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
    @Transactional
    public void toggleVisibility(Long documentId) {
        DocDocument doc = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));
        // chỉ chủ sở hữu mới được đổi
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Bạn không có quyền đổi tài liệu này!");
        }
        doc.setVisibility(
                doc.getVisibility() == DocumentVisibility.PUBLIC
                        ? DocumentVisibility.PRIVATE
                        : DocumentVisibility.PUBLIC
        );
        docDocumentRepository.save(doc);
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
        if (doc.getFiles() != null && !doc.getFiles().isEmpty()) {
            dto.setFileUrl(doc.getFiles().getFirst().getFileUrl());
            dto.setFileType(doc.getFiles().getFirst().getFileType());
        }
        if (doc.getSubject() != null) {
            dto.setSubjectId(doc.getSubject().getId());
            dto.setSubjectName(doc.getSubject().getName());
        }
        if (doc.getTags() != null && !doc.getTags().isEmpty()) {
            dto.setTags(doc.getTags().stream().map(Tag::getName).collect(Collectors.toList()));
        }

        dto.setFavoriteCount(favoriteRepository.countByDocumentId(doc.getId()));
        dto.setDownloadCount(downloadRepository.countByDocumentId(doc.getId()));
        dto.setAverageRating(ratingRepository.getAverageRatingByDocumentId(doc.getId()));

        // favorited theo user đang đăng nhập
        dto.setFavorited(favoriteRepository.existsByUserIdAndDocumentId(getCurrentUser().getId(), doc.getId()));

        List<CommentDTO> commentDTOs = commentRepository.findByDocumentIdOrderByCreatedAtDesc(doc.getId()).stream()
                .map(c -> new CommentDTO(c.getId(), c.getUser().getFullName(), c.getContent(), c.getCreatedAt()))
                .collect(Collectors.toList());
        dto.setComments(commentDTOs);

        return dto;
    }
    public DocumentResponseDTO getDocumentById(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));
        return mapToResponseDTO(doc);
    }
}