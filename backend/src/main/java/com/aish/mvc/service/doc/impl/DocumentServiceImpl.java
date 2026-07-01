package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.CommentDTO;
import com.aish.mvc.dto.doc.DocumentDownloadResult;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.*;
import com.aish.mvc.entity.enums.DocumentStatus;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.*;
import com.aish.mvc.service.doc.DocumentService;
import com.aish.mvc.service.stor.CloudUploadResult;
import com.aish.mvc.service.stor.CloudinaryService;
import com.aish.mvc.service.stor.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    @Value("${app.upload.dir}")
    private String uploadDir;

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

    private DocDocument buildDocument(String title, String description, Long subjectId, List<String> tagNames) {
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
        return docDocumentRepository.save(doc);
    }

    @Override
    @Transactional
    public DocumentResponseDTO uploadDocumentToServer(String title, String description, Long subjectId,
                                                      List<String> tagNames, MultipartFile file) {
        DocDocument savedDoc = buildDocument(title, description, subjectId, tagNames);

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
    public DocumentResponseDTO uploadDocumentToCloud(String title, String description, Long subjectId,
                                                     List<String> tagNames, MultipartFile file) {
        DocDocument savedDoc = buildDocument(title, description, subjectId, tagNames);

        CloudUploadResult uploaded = cloudinaryService.upload(file);

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
        doc.setDeletedAt(LocalDateTime.now());
        docDocumentRepository.save(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> getDeletedDocuments() {
        return docDocumentRepository.findByDeletedAtIsNotNull().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void restoreDocument(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));
        doc.setDeletedAt(null);
        docDocumentRepository.save(doc);
    }

    @Override
    public DocFile getFileByDocumentId(Long documentId) {
        DocDocument doc = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));
        return doc.getFiles().getFirst();
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

    @Override
    public DocumentResponseDTO getDocumentById(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));
        return mapToResponseDTO(doc);
    }

    @Override
    public DocumentDownloadResult prepareDownload(Long documentId) {
        DocFile docFile = getFileByDocumentId(documentId);
        logDownload(documentId);
        try {
            String fileUrl = docFile.getFileUrl();
            Resource resource;
            if (fileUrl != null && fileUrl.startsWith("http")) {
                resource = new UrlResource(new java.net.URL(fileUrl));
            } else {
                Path filePath = Paths.get(uploadDir).resolve(fileUrl).normalize();
                resource = new UrlResource(filePath.toUri());
            }
            if (!resource.exists() && !resource.isReadable()) {
                throw new RuntimeException("File không tồn tại hoặc không thể đọc");
            }
            return new DocumentDownloadResult(resource, docFile.getFileName());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi chuẩn bị file tải xuống: " + e.getMessage());
        }
    }

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
        dto.setFavorited(favoriteRepository.existsByUserIdAndDocumentId(getCurrentUser().getId(), doc.getId()));

        List<CommentDTO> commentDTOs = commentRepository.findByDocumentIdOrderByCreatedAtDesc(doc.getId()).stream()
                .map(c -> new CommentDTO(c.getId(), c.getUser().getFullName(), c.getContent(), c.getCreatedAt()))
                .collect(Collectors.toList());
        dto.setComments(commentDTOs);

        return dto;
    }
}
