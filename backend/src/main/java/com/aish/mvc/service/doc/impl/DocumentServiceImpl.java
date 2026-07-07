package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.ai.ModerationDecision;
import com.aish.mvc.dto.ai.ModerationResultDTO;
import com.aish.mvc.dto.doc.AdminDocumentSummaryDTO;
import com.aish.mvc.dto.doc.CommunityPageResponseDTO;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.*;
import com.aish.mvc.entity.enums.DocumentStatus;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.exception.ForbiddenException;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.*;
import com.aish.mvc.service.ai.AiModerationService;
import com.aish.mvc.service.doc.DocumentService;
import com.aish.mvc.service.stor.CloudinaryService;
import com.aish.mvc.service.stor.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
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
    @Autowired private AuthAccountRepository authAccountRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private AiModerationService aiModerationService;
    @Autowired private DocumentMapper documentMapper;

    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> getAllDocuments() {
        Long uid = getCurrentUser().getId();
        return docDocumentRepository.findVisibleDocuments(DocumentVisibility.PUBLIC, uid).stream()
                .map(documentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Tạo document + gắn nhiều môn học (chưa gắn file)
    private DocDocument buildDocument(String title, String description, java.util.List<Long> subjectIds) {
        // DEC-030: mỗi tài liệu phải thuộc >=1 môn học — chặn TRƯỚC khi tạo document/lưu file,
        // để không tạo ra document/file mồ côi khi validation fail.
        if (subjectIds == null || subjectIds.isEmpty()) {
            throw new IllegalArgumentException("Tài liệu phải thuộc ít nhất 1 môn học.");
        }

        Set<Subject> subjects = new HashSet<>();
        for (Long sid : subjectIds) {
            if (sid == null) continue;
            subjectRepository.findById(sid).ifPresent(subjects::add);
        }
        if (subjects.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy môn học hợp lệ nào trong danh sách đã chọn.");
        }

        DocDocument doc = new DocDocument();
        doc.setTitle(title);
        doc.setDescription(description);
        doc.setUser(getCurrentUser());
        doc.setStatus(DocumentStatus.COMPLETED);
        // DEC-006: Private by default — không kiểm duyệt lúc upload, chỉ khi user chủ động
        // chuyển sang PUBLIC (toggleVisibility) mới bắt buộc AI pre-screen (DEC-035).
        doc.setVisibility(DocumentVisibility.PRIVATE);
        doc.setModerationStatus(ModerationStatus.NOT_REQUIRED);
        doc.setSubjects(subjects);

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

        return documentMapper.toResponseDTO(savedDoc);
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

        return documentMapper.toResponseDTO(savedDoc);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> getFavoriteDocuments() {
        Long uid = getCurrentUser().getId();
        List<Long> favoriteIds = favoriteRepository.findDocumentIdsByUserId(uid);
        if (favoriteIds.isEmpty()) return List.of();
        return docDocumentRepository.findByIdInAndDeletedAtIsNull(favoriteIds).stream()
                .map(documentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("Bạn không có quyền xoá tài liệu này!");
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
                .map(documentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void restoreDocument(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("Bạn không có quyền khôi phục tài liệu này!");
        }
        doc.setDeletedAt(null);
        docDocumentRepository.save(doc);
    }

    @Override
    @Transactional
    public void permanentDeleteDocument(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("Bạn không có quyền xoá vĩnh viễn tài liệu này!");
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài liệu"));
        return doc.getFiles().getFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public DocFile getFileForPreview(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));
        boolean isOwner = doc.getUser().getId().equals(getCurrentUser().getId());
        boolean isPublic = doc.getVisibility() == DocumentVisibility.PUBLIC;
        if (!isOwner && !isPublic) {
            throw new ForbiddenException("Bạn không có quyền xem trước tài liệu này!");
        }
        if (doc.getFiles() == null || doc.getFiles().isEmpty()) {
            throw new ResourceNotFoundException("Tài liệu chưa có file!");
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

        List<DocumentResponseDTO> mapped = all.stream().map(documentMapper::toResponseDTO).collect(Collectors.toList());

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
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("Bạn không có quyền đổi tài liệu này!");
        }

        boolean goingPublic = doc.getVisibility() != DocumentVisibility.PUBLIC;

        if (!goingPublic) {
            // PUBLIC -> PRIVATE: không cần kiểm duyệt, luôn an toàn khi ẩn bớt.
            doc.setVisibility(DocumentVisibility.PRIVATE);
            docDocumentRepository.save(doc);
            return;
        }

        // -> PUBLIC: bắt buộc AI pre-screen trước (DEC-035).
        ModerationResultDTO result = aiModerationService.screen(documentId);
        doc.setModerationReason(result.getReason());

        if (result.getDecision() == ModerationDecision.PASS) {
            doc.setVisibility(DocumentVisibility.PUBLIC);
            doc.setModerationStatus(ModerationStatus.APPROVED);
        }
        else {
            // FLAG: KHÔNG public, coi như bị từ chối (REJECTED) — không còn ở trạng thái
            // "chờ" nữa. Nếu user không đồng ý, họ appeal thủ công (ModerationAppeal,
            // không qua AI) — appeal đó mới thật sự vào hàng chờ Admin (APPEAL_PENDING).
            doc.setVisibility(DocumentVisibility.PRIVATE);
            doc.setModerationStatus(ModerationStatus.REJECTED);
        }

        docDocumentRepository.save(doc);
    }

    @Override
    public DocumentResponseDTO getDocumentById(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));
        return documentMapper.toResponseDTO(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminDocumentSummaryDTO> getAllDocumentsForAdmin(Pageable pageable) {
        return docDocumentRepository.findAll(pageable).map(documentMapper::toAdminSummaryDTO);
    }

    @Override
    @Transactional
    public void adminDeleteDocument(Long id) {
        // DEC-009: admin gỡ tài liệu vi phạm nhưng KHÔNG trở thành owner — document.user
        // không bị đổi. Không kiểm tra ownership ở đây vì quyền admin đã được xác thực
        // ở tầng route (/api/admin/** -> hasRole("ADMIN")).
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));
        doc.setDeletedAt(LocalDateTime.now());
        docDocumentRepository.save(doc);
    }
}