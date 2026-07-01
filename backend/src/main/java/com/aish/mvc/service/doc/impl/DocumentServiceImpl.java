package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.CommentDTO;
import com.aish.mvc.dto.doc.CommunityPageResponseDTO;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.*;
import com.aish.mvc.entity.enums.DocumentStatus;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.*;
import com.aish.mvc.service.doc.DocumentService;
import com.aish.mvc.service.stor.CloudinaryService;
import com.aish.mvc.service.stor.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired private DocDocumentRepository docDocumentRepository;
    @Autowired private DocFileRepository docFileRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private CommentRepository commentRepository;
    @Autowired private FavoriteRepository favoriteRepository;
    @Autowired private RatingRepository ratingRepository;
    @Autowired private DownloadRepository downloadRepository;
    @Autowired private AuthUserRepository authUserRepository;
    @Autowired private AuthAccountRepository authAccountRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private CloudinaryService cloudinaryService;

    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> getAllDocuments() {
        Long uid = getCurrentUser().getId();
        return docDocumentRepository.findVisibleDocuments(DocumentVisibility.PUBLIC, uid).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Tạo document + gắn nhiều môn học (chưa gắn file)
    private DocDocument buildDocument(String title, String description, java.util.List<Long> subjectIds) {
        DocDocument doc = new DocDocument();
        doc.setTitle(title);
        doc.setDescription(description);
        doc.setUser(getCurrentUser());
        doc.setStatus(DocumentStatus.COMPLETED);
        doc.setVisibility(DocumentVisibility.PUBLIC);

        if (subjectIds != null && !subjectIds.isEmpty()) {
            Set<Subject> subjects = new HashSet<>();
            for (Long sid : subjectIds) {
                if (sid == null) continue;
                subjectRepository.findById(sid).ifPresent(subjects::add);
            }
            doc.setSubjects(subjects);
        }
        return docDocumentRepository.save(doc);
    }

    @Override
    @Transactional
    public DocumentResponseDTO uploadDocumentToServer(String title, String description, java.util.List<Long> subjectIds, MultipartFile file) {
        DocDocument savedDoc = buildDocument(title, description, subjectIds);

        String storedFileName = fileStorageService.storeFile(file);

        DocFile docFile = DocFile.builder()
                .fileName(file.getOriginalFilename())
                .fileUrl(storedFileName)
                .resourceType("local")
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .document(savedDoc)
                .build();
        savedDoc.addFile(docFile);
        docFileRepository.save(docFile);

        return mapToResponseDTO(savedDoc);
    }

    @Override
    @Transactional
    public DocumentResponseDTO uploadDocumentToCloud(String title, String description, java.util.List<Long> subjectIds, MultipartFile file) {
        DocDocument savedDoc = buildDocument(title, description, subjectIds);

        com.aish.mvc.service.stor.CloudUploadResult uploaded = cloudinaryService.upload(file);

        DocFile docFile = DocFile.builder()
                .fileName(file.getOriginalFilename())
                .fileUrl(uploaded.url())
                .publicId(uploaded.publicId())
                .resourceType(uploaded.resourceType())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .document(savedDoc)
                .build();
        savedDoc.addFile(docFile);
        docFileRepository.save(docFile);

        return mapToResponseDTO(savedDoc);
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Bạn không có quyền xoá tài liệu này!");
        }
        doc.setDeletedAt(LocalDateTime.now());
        docDocumentRepository.save(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> getDeletedDocuments() {
        Long uid = getCurrentUser().getId();
        // CHỈ trả về rác của user đang đăng nhập (trước đây bị lỗi trả về rác của TẤT CẢ user)
        return docDocumentRepository.findByDeletedAtIsNotNullAndUser_Id(uid).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void restoreDocument(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Bạn không có quyền khôi phục tài liệu này!");
        }
        doc.setDeletedAt(null);
        docDocumentRepository.save(doc);
    }

    @Override
    @Transactional
    public void permanentDeleteDocument(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new RuntimeException("Bạn không có quyền xoá vĩnh viễn tài liệu này!");
        }

        if (doc.getFiles() != null) {
            for (DocFile f : doc.getFiles()) {
                if ("local".equals(f.getResourceType())) {
                    fileStorageService.deleteFile(f.getFileUrl());
                } else if (f.getPublicId() != null) {
                    try { cloudinaryService.delete(f.getPublicId(), f.getResourceType()); }
                    catch (Exception ignored) {}
                }
            }
        }

        commentRepository.deleteByDocumentId(id);
        ratingRepository.deleteByDocumentId(id);
        favoriteRepository.deleteByDocumentId(id);
        downloadRepository.deleteByDocumentId(id);

        docDocumentRepository.delete(doc);
    }

    @Override
    public DocFile getFileByDocumentId(Long documentId) {
        DocDocument doc = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));
        return doc.getFiles().getFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public DocFile getFileForPreview(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));
        boolean isOwner = doc.getUser().getId().equals(getCurrentUser().getId());
        boolean isPublic = doc.getVisibility() == DocumentVisibility.PUBLIC;
        if (!isOwner && !isPublic) {
            throw new RuntimeException("Bạn không có quyền xem trước tài liệu này!");
        }
        if (doc.getFiles() == null || doc.getFiles().isEmpty()) {
            throw new RuntimeException("Tài liệu chưa có file!");
        }
        return doc.getFiles().getFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityPageResponseDTO getCommunityDocuments(String keyword, Long subjectId, String sortBy, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 12;

        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        List<DocDocument> all = docDocumentRepository.findCommunityDocuments(DocumentVisibility.PUBLIC, kw, subjectId);

        List<DocumentResponseDTO> mapped = all.stream().map(this::mapToResponseDTO).collect(Collectors.toList());

        java.util.Comparator<DocumentResponseDTO> comparator;
        if ("downloads".equalsIgnoreCase(sortBy)) {
            comparator = java.util.Comparator.comparing(
                    (DocumentResponseDTO d) -> d.getDownloadCount() == null ? 0L : d.getDownloadCount()
            ).reversed();
        } else if ("rating".equalsIgnoreCase(sortBy)) {
            comparator = java.util.Comparator.comparing(
                    (DocumentResponseDTO d) -> d.getAverageRating() == null ? 0.0 : d.getAverageRating()
            ).reversed();
        } else {
            // mặc định: mới nhất trước
            comparator = java.util.Comparator.comparing(
                    DocumentResponseDTO::getCreatedAt,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
            ).reversed();
        }
        mapped.sort(comparator);

        long totalItems = mapped.size();
        int totalPages = (int) Math.ceil(totalItems / (double) size);
        int fromIndex = Math.min(page * size, mapped.size());
        int toIndex = Math.min(fromIndex + size, mapped.size());
        List<DocumentResponseDTO> pageItems = mapped.subList(fromIndex, toIndex);

        return new CommunityPageResponseDTO(pageItems, page, size, totalItems, totalPages);
    }

    @Override
    @Transactional
    public void toggleVisibility(Long documentId) {
        DocDocument doc = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));
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

    private DocumentResponseDTO mapToResponseDTO(DocDocument doc) {
        DocumentResponseDTO dto = new DocumentResponseDTO();
        dto.setId(doc.getId());
        dto.setTitle(doc.getTitle());
        dto.setDescription(doc.getDescription());
        dto.setStatus(doc.getStatus() != null ? doc.getStatus().name() : "COMPLETED");
        dto.setVisibility(doc.getVisibility() != null ? doc.getVisibility().name() : "PUBLIC");
        dto.setCreatedAt(doc.getCreatedAt());
        dto.setDeletedAt(doc.getDeletedAt());

        if (doc.getUser() != null) dto.setOwnerName(doc.getUser().getFullName());
        if (doc.getFiles() != null && !doc.getFiles().isEmpty()) {
            dto.setFileName(doc.getFiles().getFirst().getFileName());
            dto.setFileUrl(doc.getFiles().getFirst().getFileUrl());
            dto.setFileType(doc.getFiles().getFirst().getFileType());
        }
        if (doc.getSubjects() != null && !doc.getSubjects().isEmpty()) {
            dto.setSubjectIds(doc.getSubjects().stream().map(Subject::getId).collect(Collectors.toList()));
            dto.setSubjectNames(doc.getSubjects().stream().map(Subject::getName).collect(Collectors.toList()));
        }

        dto.setFavoriteCount(favoriteRepository.countByDocumentId(doc.getId()));
        dto.setDownloadCount(downloadRepository.countByDocumentId(doc.getId()));
        dto.setAverageRating(ratingRepository.getAverageRatingByDocumentId(doc.getId()));
        dto.setFavorited(favoriteRepository.existsByUserIdAndDocumentId(getCurrentUser().getId(), doc.getId()));

        List<CommentDTO> commentDTOs = commentRepository.findByDocumentIdOrderByCreatedAtDesc(doc.getId()).stream()
                .map(c -> new CommentDTO(c.getId(), c.getUser().getFullName(), c.getContent(), c.getCreatedAt()))
                .collect(Collectors.toList());
        dto.setComments(commentDTOs);

        return dto;
    }

    @Override
    public DocumentResponseDTO getDocumentById(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));
        return mapToResponseDTO(doc);
    }
}