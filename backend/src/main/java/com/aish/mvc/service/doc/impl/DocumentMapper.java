package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.AdminDocumentSummaryDTO;
import com.aish.mvc.dto.doc.CommentDTO;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.entity.doc.Subject;
import com.aish.mvc.entity.enums.IngestStatus;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.CommentRepository;
import com.aish.mvc.repository.doc.DownloadRepository;
import com.aish.mvc.repository.doc.FavoriteRepository;
import com.aish.mvc.repository.doc.RatingRepository;
import com.aish.mvc.service.doc.DocEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DocumentMapper {

    private final FavoriteRepository favoriteRepository;
    private final DownloadRepository downloadRepository;
    private final RatingRepository ratingRepository;
    private final CommentRepository commentRepository;
    private final AuthAccountRepository authAccountRepository;
    private final DocEmbeddingService docEmbeddingService;

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"))
                .getUser().getId();
    }

    // DocFile.resourceType: "local" (upload-server) | "image"/"raw" (Cloudinary) -> LOCAL/CLOUD cho FE (DEC-031).
    public String toStorageType(String resourceType) {
        return "local".equalsIgnoreCase(resourceType) ? "LOCAL" : "CLOUD";
    }

    public DocumentResponseDTO toResponseDTO(DocDocument doc) {
        DocumentResponseDTO dto = new DocumentResponseDTO();
        dto.setId(doc.getId());
        dto.setTitle(doc.getTitle());
        dto.setDescription(doc.getDescription());
        dto.setStatus(doc.getStatus() != null ? doc.getStatus().name() : "COMPLETED");
        dto.setVisibility(doc.getVisibility() != null ? doc.getVisibility().name() : "PUBLIC");
        dto.setModerationStatus(doc.getModerationStatus() != null ? doc.getModerationStatus().name() : ModerationStatus.NOT_REQUIRED.name());
        dto.setModerationReason(doc.getModerationReason());
        dto.setIngestStatus(doc.getIngestStatus() != null ? doc.getIngestStatus().name() : IngestStatus.NOT_INGESTED.name());
        dto.setCreatedAt(doc.getCreatedAt());
        dto.setDeletedAt(doc.getDeletedAt());

        if (doc.getUser() != null) dto.setOwnerName(doc.getUser().getFullName());
        if (doc.getFiles() != null && !doc.getFiles().isEmpty()) {
            DocFile primaryFile = doc.getFiles().getFirst();
            dto.setFileName(primaryFile.getFileName());
            dto.setFileUrl(primaryFile.getFileUrl());
            dto.setFileType(primaryFile.getFileType());
            dto.setStorageType(toStorageType(primaryFile.getResourceType()));
            dto.setAiSupported(docEmbeddingService.isAiSupported(primaryFile.getFileName(), primaryFile.getFileType()));
        } else {
            dto.setAiSupported(false);
        }
        if (doc.getSubjects() != null && !doc.getSubjects().isEmpty()) {
            dto.setSubjectIds(doc.getSubjects().stream().map(Subject::getId).collect(Collectors.toList()));
            dto.setSubjectNames(doc.getSubjects().stream().map(Subject::getName).collect(Collectors.toList()));
        }

        dto.setFavoriteCount(favoriteRepository.countByDocumentId(doc.getId()));
        dto.setDownloadCount(downloadRepository.countByDocumentId(doc.getId()));
        dto.setAverageRating(ratingRepository.getAverageRatingByDocumentId(doc.getId()));
        dto.setFavorited(favoriteRepository.existsByUserIdAndDocumentId(currentUserId(), doc.getId()));

        List<CommentDTO> commentDTOs = commentRepository.findByDocumentIdOrderByCreatedAtDesc(doc.getId()).stream()
                .map(c -> new CommentDTO(c.getId(), c.getUser().getFullName(), c.getContent(), c.getCreatedAt()))
                .collect(Collectors.toList());
        dto.setComments(commentDTOs);

        return dto;
    }

    public AdminDocumentSummaryDTO toAdminSummaryDTO(DocDocument doc) {
        String storageType = doc.getFiles() != null && !doc.getFiles().isEmpty()
                ? toStorageType(doc.getFiles().getFirst().getResourceType())
                : null;
        return new AdminDocumentSummaryDTO(
                doc.getId(),
                doc.getTitle(),
                doc.getUser() != null ? doc.getUser().getFullName() : null,
                doc.getVisibility() != null ? doc.getVisibility().name() : null,
                doc.getModerationStatus() != null ? doc.getModerationStatus().name() : ModerationStatus.NOT_REQUIRED.name(),
                storageType,
                doc.getCreatedAt());
    }
}
