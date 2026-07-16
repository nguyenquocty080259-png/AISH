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
import com.aish.mvc.entity.enums.NotificationType;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.exception.ForbiddenException;
import com.aish.mvc.exception.ResourceNotFoundException;
import com.aish.mvc.repository.ai.AiConversationRepository;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.*;
import com.aish.mvc.service.ai.AiModerationService;
import com.aish.mvc.service.doc.DocumentService;
import com.aish.mvc.service.doc.NamingModerationService;
import com.aish.mvc.service.doc.DocumentContentKeywordService;
import com.aish.mvc.service.notification.NotificationService;
import com.aish.mvc.service.stor.CloudinaryService;
import com.aish.mvc.service.stor.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);

    @Autowired private DocDocumentRepository docDocumentRepository;
    @Autowired private DocFileRepository docFileRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private CommentRepository commentRepository;
    @Autowired private FavoriteRepository favoriteRepository;
    @Autowired private RatingRepository ratingRepository;
    @Autowired private DownloadRepository downloadRepository;
    @Autowired private CollectionItemRepository collectionItemRepository;
    @Autowired private DocEmbeddingRepository docEmbeddingRepository;
    @Autowired private ModerationAppealRepository moderationAppealRepository;
    @Autowired private ViewHistoryRepository viewHistoryRepository;
    @Autowired private AiConversationRepository aiConversationRepository;
    @Autowired private AuthAccountRepository authAccountRepository;
    @Autowired private AuthUserRepository authUserRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private com.aish.mvc.service.stor.ThumbnailService thumbnailService;
    @Autowired private AiModerationService aiModerationService;
    @Autowired private DocumentMapper documentMapper;
    @Autowired private NotificationService notificationService;
    @Autowired private NamingModerationService namingModerationService;
    @Autowired private DocumentContentKeywordService documentContentKeywordService;

    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    // Mode "CẢ HAI" có 2 bản (local + cloud) — luôn ưu tiên bản local vì đọc nhanh
    // và không dính hạn chế deliver của Cloudinary (PDF trên account free bị 401).
    private static DocFile pickPrimaryFile(DocDocument doc) {
        return doc.getFiles().stream()
                .filter(f -> "local".equalsIgnoreCase(f.getResourceType()))
                .findFirst()
                .orElse(doc.getFiles().getFirst());
    }

    // getAllDocuments (My Documents - chỉ của mình, chưa xóa):
    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> getAllDocuments() {
        Long uid = getCurrentUser().getId();
        return docDocumentRepository.findByDeletedAtIsNullAndUser_Id(uid).stream()
                .map(documentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Tạo document + gắn nhiều môn học (chưa gắn file)
    private DocDocument buildDocument(String title, String description, java.util.List<Long> subjectIds) {
        String validatedTitle = namingModerationService.validate(title);
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
        doc.setTitle(validatedTitle);
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
        return uploadDocument(title, description, subjectIds, file, "LOCAL");
    }

    @Override
    @Transactional
    public DocumentResponseDTO uploadDocumentToCloud(String title, String description, java.util.List<Long> subjectIds, MultipartFile file) {
        return uploadDocument(title, description, subjectIds, file, "CLOUD");
    }

    // Upload hợp nhất: storage = "LOCAL" | "CLOUD" | "BOTH".
    // BOTH: lưu cả local lẫn Cloudinary — file local được add TRƯỚC để mọi nơi đọc
    // (preview/download/ingest AI) ưu tiên bản local, không phụ thuộc Cloudinary.
    @Override
    @Transactional
    public DocumentResponseDTO uploadDocument(String title, String description, java.util.List<Long> subjectIds, MultipartFile file, String storage) {
        String target = storage == null ? "LOCAL" : storage.trim().toUpperCase();
        if (!Set.of("LOCAL", "CLOUD", "BOTH").contains(target)) {
            throw new IllegalArgumentException("storage phải là LOCAL, CLOUD hoặc BOTH (nhận được: " + storage + ")");
        }

        DocDocument savedDoc = buildDocument(title, description, subjectIds);

        // Thumbnail sinh 1 lần cho cả 2 bản (best-effort, null nếu định dạng không hỗ trợ).
        String thumbnailUrl = thumbnailService.createThumbnail(file);

        if ("LOCAL".equals(target) || "BOTH".equals(target)) {
            String storedFileName = fileStorageService.storeFile(file);
            DocFile localFile = DocFile.builder()
                    .fileName(file.getOriginalFilename())
                    .fileUrl(storedFileName)
                    .resourceType("local")
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .thumbnailUrl(thumbnailUrl)
                    .document(savedDoc)
                    .build();
            savedDoc.addFile(localFile);
            docFileRepository.save(localFile);
        }

        if ("CLOUD".equals(target) || "BOTH".equals(target)) {
            com.aish.mvc.service.stor.CloudUploadResult uploaded = cloudinaryService.upload(file);
            DocFile cloudFile = DocFile.builder()
                    .fileName(file.getOriginalFilename())
                    .fileUrl(uploaded.url())
                    .publicId(uploaded.publicId())
                    .resourceType(uploaded.resourceType())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .thumbnailUrl(thumbnailUrl)
                    .document(savedDoc)
                    .build();
            savedDoc.addFile(cloudFile);
            docFileRepository.save(cloudFile);
        }

        return documentMapper.toResponseDTO(savedDoc);
    }

    // Áp title/description/subjectIds vào doc — dùng chung cho updateDocument (owner) và
    // adminUpdateDocument (admin), chỉ khác nhau ở check ownership phía trên.
    private void applyDocumentUpdate(DocDocument doc, String title, String description, java.util.List<Long> subjectIds) {
        if (title != null && !title.isBlank()) doc.setTitle(title.trim());
        if (description != null) doc.setDescription(description);

        // subjectIds == null: giữ nguyên. Nếu gửi thì bắt buộc có >=1 môn hợp lệ (DEC-030).
        if (subjectIds != null) {
            Set<Subject> subjects = new HashSet<>();
            for (Long sid : subjectIds) {
                if (sid == null) continue;
                subjectRepository.findById(sid).ifPresent(subjects::add);
            }
            if (subjects.isEmpty()) {
                throw new IllegalArgumentException("Tài liệu phải thuộc ít nhất 1 môn học hợp lệ.");
            }
            doc.setSubjects(subjects);
        }
    }

    @Override
    @Transactional
    public DocumentResponseDTO updateDocument(Long id, String title, String description, java.util.List<Long> subjectIds) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("Bạn không có quyền sửa tài liệu này!");
        }

        String validatedTitle = title == null ? null : namingModerationService.validate(title);
        applyDocumentUpdate(doc, validatedTitle, description, subjectIds);

        return documentMapper.toResponseDTO(docDocumentRepository.save(doc));
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
                thumbnailService.deleteThumbnail(f.getThumbnailUrl());
            }
        }

        commentRepository.deleteByDocumentId(id);
        ratingRepository.deleteByDocumentId(id);
        favoriteRepository.deleteByDocumentId(id);
        downloadRepository.deleteByDocumentId(id);
        collectionItemRepository.deleteByDocumentId(id);
        docEmbeddingRepository.deleteByDocument_Id(id);
        moderationAppealRepository.deleteByDocumentId(id);
        viewHistoryRepository.deleteByDocumentId(id);
        aiConversationRepository.clearDocumentReference(id);

        docDocumentRepository.delete(doc);
    }

    @Override
    public DocFile getFileByDocumentId(Long documentId) {
        DocDocument doc = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài liệu"));
        return pickPrimaryFile(doc);
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
        return pickPrimaryFile(doc);
    }

    // getCommunityDocuments - đổi đầu method:
    public CommunityPageResponseDTO getCommunityDocuments(String keyword, Long subjectId, Long tagId, Double minRating, String sortBy, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 12;

        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        List<DocDocument> all = docDocumentRepository.findCommunityDocuments(DocumentVisibility.PUBLIC, kw, subjectId, tagId);
        // ... phần còn lại giữ nguyên (minRating + sort + phân trang)
        List<DocumentResponseDTO> mapped = all.stream().map(documentMapper::toResponseDTO).collect(Collectors.toList());

        // Lọc theo điểm trung bình tối thiểu (tính ở mapper) — làm sau khi map vì avgRating không nằm trong bảng documents.
        if (minRating != null) {
            mapped = mapped.stream()
                    .filter(d -> d.getAverageRating() != null && d.getAverageRating() >= minRating)
                    .collect(Collectors.toList());
        }

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
    public DocumentResponseDTO toggleVisibility(Long documentId) {
        DocDocument doc = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("Bạn không có quyền đổi tài liệu này!");
        }

        boolean goingPublic = doc.getVisibility() != DocumentVisibility.PUBLIC;

        if (!goingPublic) {
            // PUBLIC -> PRIVATE: không cần kiểm duyệt, luôn an toàn khi ẩn bớt.
            doc.setVisibility(DocumentVisibility.PRIVATE);
            return documentMapper.toResponseDTO(docDocumentRepository.save(doc));
        }

        // -> PUBLIC: reset lớp admin review trước mọi loại pre-screen.
        doc.setAdminReviewedAt(null);
        doc.setAdminReviewedBy(null);

        // Keyword DB là lớp rẻ nhất. Hit thì tự động từ chối và không gọi AI.
        if (documentContentKeywordService.matches(doc)) {
            log.info("Document {} matched a DOCUMENT_CONTENT keyword; skipping AI moderation screen", documentId);
            doc.setVisibility(DocumentVisibility.PRIVATE);
            doc.setModerationStatus(ModerationStatus.REJECTED);
            doc.setModerationReason("Nội dung tài liệu kích hoạt quy tắc từ khóa không phù hợp.");
            DocDocument rejected = docDocumentRepository.save(doc);
            notifyAdminsDocumentScreened(rejected, "REJECTED_BY_CONTENT_KEYWORD");
            return documentMapper.toResponseDTO(rejected);
        }

        // Không trúng keyword: tiếp tục AI pre-screen hiện có (DEC-035).
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

        DocDocument savedDocument = docDocumentRepository.save(doc);
        notifyAdminsDocumentScreened(savedDocument, result.getDecision().name());

        return documentMapper.toResponseDTO(savedDocument);
    }

    private void notifyAdminsDocumentScreened(DocDocument document, String outcome) {
        try {
            String message = "Tài liệu \"" + document.getTitle()
                    + "\" đã được sàng lọc: " + outcome;
            authUserRepository.findByRole_RoleNameAndStatus("ADMIN", UserStatus.ACTIVE)
                    .forEach(admin -> notificationService.createDocumentNotification(
                            admin.getId(),
                            NotificationType.DOCUMENT_SCREENED,
                            message,
                            document.getId()));
        } catch (Exception exception) {
            log.error("Không thể gửi thông báo kiểm duyệt tài liệu id={} cho Admin; publish vẫn tiếp tục.",
                    document.getId(), exception);
        }
    }

    @Override
    public DocumentResponseDTO getDocumentById(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));
        return documentMapper.toResponseDTO(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminDocumentSummaryDTO> getAllDocumentsForAdmin(
            DocumentVisibility visibility,
            boolean needsReview,
            Pageable pageable) {

        Page<DocDocument> page;

        if (needsReview) {

            page = docDocumentRepository
                    .findByModerationStatusInAndAdminReviewedAtIsNullAndDeletedAtIsNull(
                            List.of(ModerationStatus.APPROVED, ModerationStatus.REJECTED),
                            pageable);

        } else if (visibility == null) {

            page = docDocumentRepository.findAll(pageable);

        } else {

            page = docDocumentRepository
                    .findByVisibility(visibility, pageable);

        }
        return page.map(document -> {

            String storageType = document.getFiles().isEmpty()
                    ? "-"
                    : document.getFiles().get(0).getResourceType();

            return new AdminDocumentSummaryDTO(
                    document.getId(),
                    document.getTitle(),
                    document.getUser().getFullName(),
                    document.getVisibility().name(),
                    document.getModerationStatus().name(),
                    storageType,
                    document.getCreatedAt(),
                    document.getIngestStatus() != null ? document.getIngestStatus().name() : null,
                    document.getAdminReviewedAt(),
                    document.getDeletedAt()
            );
        });
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

    @Override
    @Transactional
    public DocumentResponseDTO adminUpdateDocument(Long id, String title, String description, java.util.List<Long> subjectIds) {
        // DEC-009: admin sửa metadata tài liệu nhưng KHÔNG trở thành owner — document.user
        // không bị đổi. Không kiểm tra ownership ở đây vì quyền admin đã được xác thực
        // ở tầng route (/api/admin/** -> hasRole("ADMIN")).
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));
        applyDocumentUpdate(doc, title, description, subjectIds);
        return documentMapper.toResponseDTO(docDocumentRepository.save(doc));
    }

    @Override
    @Transactional
    public void adminRestoreDocument(Long id) {
        // DEC-009: admin khôi phục tài liệu đã gỡ nhưng KHÔNG trở thành owner — document.user
        // không bị đổi. Không kiểm tra ownership ở đây vì quyền admin đã được xác thực
        // ở tầng route (/api/admin/** -> hasRole("ADMIN")).
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tồn tại!"));
        doc.setDeletedAt(null);
        docDocumentRepository.save(doc);
    }
}
