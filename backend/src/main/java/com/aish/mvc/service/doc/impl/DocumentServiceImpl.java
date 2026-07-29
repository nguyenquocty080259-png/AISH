package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.ai.ModerationDecision;
import com.aish.mvc.dto.ai.ModerationResultDTO;
import com.aish.mvc.dto.doc.AdminDocumentSummaryDTO;
import com.aish.mvc.dto.doc.CommunityPageResponseDTO;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.dto.doc.StorageUsageDTO;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.auth.AuthUserProfile;
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
import com.aish.mvc.repository.auth.AuthUserProfileRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.*;
import com.aish.mvc.service.ai.AiModerationService;
import com.aish.mvc.service.config.SystemSettingService;
import com.aish.mvc.service.doc.DocumentService;
import com.aish.mvc.service.doc.DocumentShareService;
import com.aish.mvc.service.doc.RoleNames;
import com.aish.mvc.service.doc.StorageTarget;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);

    // Phân trang trang Cộng đồng: mặc định khớp với lưới 12 thẻ của FE, trần chặn request cố ý
    // xin cả kho tài liệu trong một lần gọi.
    static final int DEFAULT_COMMUNITY_PAGE_SIZE = 12;
    static final int MAX_COMMUNITY_PAGE_SIZE = 50;

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
    @Autowired private DocumentShareRepository documentShareRepository;
    @Autowired private AiConversationRepository aiConversationRepository;
    @Autowired private AuthAccountRepository authAccountRepository;
    @Autowired private AuthUserRepository authUserRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private com.aish.mvc.service.stor.ThumbnailService thumbnailService;
    @Autowired private com.aish.mvc.service.stor.UploadFileTypeService uploadFileTypeService;
    @Autowired private AiModerationService aiModerationService;
    @Autowired private DocumentMapper documentMapper;
    @Autowired private DocumentShareService documentShareService;
    @Autowired private NotificationService notificationService;
    @Autowired private NamingModerationService namingModerationService;
    @Autowired private DocumentContentKeywordService documentContentKeywordService;
    @Autowired private AuthUserProfileRepository authUserProfileRepository;
    @Autowired private SystemSettingService systemSettingService;

    private AuthUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user đang đăng nhập"))
                .getUser();
    }

    // ADMIN được xem chi tiết / xem trước / tải NỘI DUNG mọi tài liệu (kể cả PRIVATE của người
    // khác) để phục vụ kiểm duyệt — cùng kiểu miễn trừ theo role đã dùng ở các luồng khác trong
    // service này (enforceMinUploadAge, DocumentMapper). CHỈ đọc role để nhận diện admin; luật
    // truy cập của user thường (chủ sở hữu / PUBLIC / được chia sẻ) giữ nguyên, không nới ra.
    private boolean isAdmin(AuthUser user) {
        return user.getRole() != null
                && RoleNames.ADMIN.equals(user.getRole().getRoleName());
    }

    // Mode "CẢ HAI" có 2 bản (local + cloud) — luôn ưu tiên bản local vì đọc nhanh
    // và không dính hạn chế deliver của Cloudinary (PDF trên account free bị 401).
    private static DocFile pickPrimaryFile(DocDocument doc) {
        // Guard nằm TRONG helper: getFirst() trên list rỗng ném NoSuchElementException (-> 500
        // "lỗi không mong muốn"), và mọi lối vào đều phải nhận cùng một câu trả lời 404 rõ ràng
        // thay vì phụ thuộc việc người gọi có nhớ tự kiểm tra hay không.
        List<DocFile> files = doc.getFiles();
        if (files == null || files.isEmpty()) {
            throw new ResourceNotFoundException("error.document.noFile");
        }
        return files.stream()
                .filter(f -> DocFile.RESOURCE_TYPE_LOCAL.equalsIgnoreCase(f.getResourceType()))
                .findFirst()
                .orElse(files.getFirst());
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

    // Chặn upload nếu người dùng chưa đủ tuổi tối thiểu (system_settings.MIN_UPLOAD_AGE, mặc
    // định 16, chỉnh tại /admin/settings). ADMIN được miễn, cùng kiểu miễn trừ role đã dùng ở
    // các luồng khác trong service này (DocumentMapper.isAdmin, MetadataSuggestionService...).
    private void enforceMinUploadAge() {
        AuthUser currentUser = getCurrentUser();
        boolean isAdmin = currentUser.getRole() != null
                && RoleNames.ADMIN.equals(currentUser.getRole().getRoleName());
        if (isAdmin) return;

        LocalDate dob = authUserProfileRepository.findByUserId(currentUser.getId())
                .map(AuthUserProfile::getDob)
                .orElse(null);
        if (dob == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "error.document.dobRequired");
        }

        int age = Period.between(dob, LocalDate.now()).getYears();
        int minAge = systemSettingService.getInt(
                SystemSettingService.MIN_UPLOAD_AGE_KEY, SystemSettingService.MIN_UPLOAD_AGE_DEFAULT);
        if (age < minAge) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Bạn cần đủ " + minAge + " tuổi để tải tài liệu lên.");
        }
    }

    // Chặn upload vượt giới hạn dung lượng MỘT FILE, cấu hình theo nơi lưu (system_settings.
    // MAX_FILE_LOCAL_BYTES / MAX_FILE_CLOUD_BYTES, mặc định 200 MiB, chỉnh tại /admin/settings).
    // BOTH phải vượt qua CẢ HAI giới hạn vì file được lưu ở cả 2 nơi. Package-private để unit
    // test gọi trực tiếp mà không cần dựng lại toàn bộ pipeline buildDocument()/file storage.
    void enforceUploadSizeLimit(long fileSize, String storage) {
        if (StorageTarget.LOCAL.equals(storage) || StorageTarget.BOTH.equals(storage)) {
            long maxLocal = systemSettingService.getLong(
                    SystemSettingService.MAX_FILE_LOCAL_BYTES_KEY, SystemSettingService.MAX_FILE_LOCAL_BYTES_DEFAULT);
            if (fileSize > maxLocal) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Tệp " + humanReadableSize(fileSize) + " vượt giới hạn " + humanReadableSize(maxLocal)
                                + " cho nơi lưu LOCAL.");
            }
        }
        if (StorageTarget.CLOUD.equals(storage) || StorageTarget.BOTH.equals(storage)) {
            long maxCloud = systemSettingService.getLong(
                    SystemSettingService.MAX_FILE_CLOUD_BYTES_KEY, SystemSettingService.MAX_FILE_CLOUD_BYTES_DEFAULT);
            if (fileSize > maxCloud) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Tệp " + humanReadableSize(fileSize) + " vượt giới hạn " + humanReadableSize(maxCloud)
                                + " cho nơi lưu CLOUD.");
            }
        }
    }

    // Chặn upload vượt TỔNG QUOTA dung lượng của người dùng (system_settings.QUOTA_LOCAL_BYTES /
    // QUOTA_CLOUD_BYTES, mặc định 1 GiB, chung cho mọi user - không có override theo từng
    // người). Dùng đã dùng tính CẢ tài liệu trong thùng rác (xem DocFileRepository) - đây là
    // chủ ý: bytes vẫn còn chiếm disk/Cloudinary tới khi xoá vĩnh viễn. BOTH phải vượt qua CẢ
    // HAI quota, kiểm tra độc lập (không dừng sớm sau khi 1 bên qua). Package-private để test.
    void enforceUploadQuota(long fileSize, String storage, Long userId) {
        if (StorageTarget.LOCAL.equals(storage) || StorageTarget.BOTH.equals(storage)) {
            long usedLocal = docFileRepository.sumLocalFileSizeByUserId(userId);
            long quotaLocal = systemSettingService.getLong(
                    SystemSettingService.QUOTA_LOCAL_BYTES_KEY, SystemSettingService.QUOTA_LOCAL_BYTES_DEFAULT);
            if (usedLocal + fileSize > quotaLocal) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Dung lượng đã dùng " + humanReadableSize(usedLocal) + " + tệp " + humanReadableSize(fileSize)
                                + " vượt quota " + humanReadableSize(quotaLocal) + " cho nơi lưu LOCAL.");
            }
        }
        if (StorageTarget.CLOUD.equals(storage) || StorageTarget.BOTH.equals(storage)) {
            long usedCloud = docFileRepository.sumCloudFileSizeByUserId(userId);
            long quotaCloud = systemSettingService.getLong(
                    SystemSettingService.QUOTA_CLOUD_BYTES_KEY, SystemSettingService.QUOTA_CLOUD_BYTES_DEFAULT);
            if (usedCloud + fileSize > quotaCloud) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Dung lượng đã dùng " + humanReadableSize(usedCloud) + " + tệp " + humanReadableSize(fileSize)
                                + " vượt quota " + humanReadableSize(quotaCloud) + " cho nơi lưu CLOUD.");
            }
        }
    }

    private static String humanReadableSize(long bytes) {
        double gb = bytes / (1024.0 * 1024 * 1024);
        if (gb >= 1) return String.format("%.1f GB", gb);
        double mb = bytes / (1024.0 * 1024);
        if (mb >= 1) return String.format("%.1f MB", mb);
        return String.format("%.1f KB", bytes / 1024.0);
    }

    // Đổi danh sách id môn học -> tập Subject, dùng chung cho lúc tạo và lúc sửa tài liệu.
    // Trước đây id không tồn tại bị ifPresent() nuốt im lặng: người dùng chọn 3 môn, chỉ 1 môn
    // được lưu, không có lỗi nào báo về — dữ liệu sai mà cả hai phía đều tưởng là đã lưu đủ.
    Set<Subject> resolveSubjects(java.util.List<Long> subjectIds) {
        Set<Subject> subjects = new HashSet<>();
        java.util.List<Long> missing = new java.util.ArrayList<>();
        for (Long sid : subjectIds) {
            if (sid == null) continue;
            subjectRepository.findById(sid).ifPresentOrElse(subjects::add, () -> missing.add(sid));
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy môn học với id: " + missing);
        }
        if (subjects.isEmpty()) {
            throw new IllegalArgumentException("error.document.subjectRequiredValid");
        }
        return subjects;
    }

    // Tạo document + gắn nhiều môn học (chưa gắn file)
    private DocDocument buildDocument(String title, String description, java.util.List<Long> subjectIds) {
        String validatedTitle = namingModerationService.validate(title);
        // DEC-030: mỗi tài liệu phải thuộc >=1 môn học — chặn TRƯỚC khi tạo document/lưu file,
        // để không tạo ra document/file mồ côi khi validation fail.
        if (subjectIds == null || subjectIds.isEmpty()) {
            throw new IllegalArgumentException("error.document.subjectRequired");
        }

        Set<Subject> subjects = resolveSubjects(subjectIds);

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
        return uploadDocument(title, description, subjectIds, file, StorageTarget.LOCAL);
    }

    @Override
    @Transactional
    public DocumentResponseDTO uploadDocumentToCloud(String title, String description, java.util.List<Long> subjectIds, MultipartFile file) {
        return uploadDocument(title, description, subjectIds, file, StorageTarget.CLOUD);
    }

    // Upload hợp nhất: storage = "LOCAL" | "CLOUD" | "BOTH".
    // BOTH: lưu cả local lẫn Cloudinary — file local được add TRƯỚC để mọi nơi đọc
    // (preview/download/ingest AI) ưu tiên bản local, không phụ thuộc Cloudinary.
    @Override
    @Transactional
    public DocumentResponseDTO uploadDocument(String title, String description, java.util.List<Long> subjectIds, MultipartFile file, String storage) {
        enforceMinUploadAge();

        String target = storage == null ? StorageTarget.LOCAL : storage.trim().toUpperCase();
        if (!StorageTarget.ALL.contains(target)) {
            throw new IllegalArgumentException("storage phải là LOCAL, CLOUD hoặc BOTH (nhận được: " + storage + ")");
        }

        // Chốt chặn loại tệp (allowlist đuôi + đối chiếu content-type thật, chống đổi đuôi).
        // Đọc allowlist tươi từ DB mỗi lần nên admin đổi là ăn liền, không cần restart.
        uploadFileTypeService.validate(file);

        enforceUploadSizeLimit(file.getSize(), target);
        enforceUploadQuota(file.getSize(), target, getCurrentUser().getId());

        DocDocument savedDoc = buildDocument(title, description, subjectIds);

        // Thumbnail sinh 1 lần cho cả 2 bản (best-effort, null nếu định dạng không hỗ trợ).
        String thumbnailUrl = thumbnailService.createThumbnail(file);

        if (StorageTarget.LOCAL.equals(target) || StorageTarget.BOTH.equals(target)) {
            String storedFileName = fileStorageService.storeFile(file);
            DocFile localFile = DocFile.builder()
                    .fileName(file.getOriginalFilename())
                    .fileUrl(storedFileName)
                    .resourceType(DocFile.RESOURCE_TYPE_LOCAL)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .thumbnailUrl(thumbnailUrl)
                    .document(savedDoc)
                    .build();
            savedDoc.addFile(localFile);
            docFileRepository.save(localFile);
        }

        if (StorageTarget.CLOUD.equals(target) || StorageTarget.BOTH.equals(target)) {
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
            doc.setSubjects(resolveSubjects(subjectIds));
        }
    }

    @Override
    @Transactional
    public DocumentResponseDTO updateDocument(Long id, String title, String description, java.util.List<Long> subjectIds) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
        // Trong thùng rác thì coi như không tồn tại, giống getDocumentById() và
        // DocumentShareServiceImpl.requireOwnedDocument() — muốn sửa thì khôi phục trước.
        if (doc.getDeletedAt() != null) {
            throw new ResourceNotFoundException("error.document.notFound");
        }
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("error.document.editForbidden");
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
    @Transactional(readOnly = true)
    public StorageUsageDTO getStorageUsage() {
        Long uid = getCurrentUser().getId();
        long usedLocal = docFileRepository.sumLocalFileSizeByUserId(uid);
        long usedCloud = docFileRepository.sumCloudFileSizeByUserId(uid);
        long quotaLocal = systemSettingService.getLong(
                SystemSettingService.QUOTA_LOCAL_BYTES_KEY, SystemSettingService.QUOTA_LOCAL_BYTES_DEFAULT);
        long quotaCloud = systemSettingService.getLong(
                SystemSettingService.QUOTA_CLOUD_BYTES_KEY, SystemSettingService.QUOTA_CLOUD_BYTES_DEFAULT);
        long maxFileLocal = systemSettingService.getLong(
                SystemSettingService.MAX_FILE_LOCAL_BYTES_KEY, SystemSettingService.MAX_FILE_LOCAL_BYTES_DEFAULT);
        long maxFileCloud = systemSettingService.getLong(
                SystemSettingService.MAX_FILE_CLOUD_BYTES_KEY, SystemSettingService.MAX_FILE_CLOUD_BYTES_DEFAULT);
        return new StorageUsageDTO(usedLocal, usedCloud, quotaLocal, quotaCloud, maxFileLocal, maxFileCloud);
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("error.document.deleteForbidden");
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
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("error.document.restoreForbidden");
        }
        doc.setDeletedAt(null);
        docDocumentRepository.save(doc);
    }

    @Override
    @Transactional
    public void permanentDeleteDocument(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("error.document.permanentDeleteForbidden");
        }

        if (doc.getFiles() != null) {
            for (DocFile f : doc.getFiles()) {
                if (DocFile.RESOURCE_TYPE_LOCAL.equals(f.getResourceType())) {
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
        documentShareRepository.deleteByDocumentId(id);
        aiConversationRepository.clearDocumentReference(id);

        docDocumentRepository.delete(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public DocFile getFileByDocumentId(Long documentId) {
        DocDocument doc = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
        // Tải file là một kênh lấy NỘI DUNG như xem trước — phải áp cùng luật truy cập với
        // getFileForPreview(): chủ sở hữu / PUBLIC / được chia sẻ. Nếu không, người vừa bị GỠ
        // chia sẻ vẫn tải được nội dung (rò rỉ, phá vỡ cách ly của tính năng chia sẻ).
        AuthUser currentUser = getCurrentUser();
        Long currentUserId = currentUser.getId();
        boolean isOwner = doc.getUser().getId().equals(currentUserId);
        boolean isPublic = doc.getVisibility() == DocumentVisibility.PUBLIC;
        boolean isShared = documentShareService.hasShareAccess(documentId, currentUserId);
        if (!isOwner && !isPublic && !isShared && !isAdmin(currentUser)) {
            throw new ForbiddenException("error.document.downloadForbidden");
        }
        return pickPrimaryFile(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public DocFile getFileForPreview(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
        AuthUser currentUser = getCurrentUser();
        Long currentUserId = currentUser.getId();
        boolean isOwner = doc.getUser().getId().equals(currentUserId);
        boolean isPublic = doc.getVisibility() == DocumentVisibility.PUBLIC;
        // Người được chia sẻ (RESTRICTED theo userId, hoặc ANYONE_WITH_LINK) cũng được xem trước.
        boolean isShared = documentShareService.hasShareAccess(id, currentUserId);
        if (!isOwner && !isPublic && !isShared && !isAdmin(currentUser)) {
            throw new ForbiddenException("error.document.previewForbidden");
        }
        return pickPrimaryFile(doc);
    }

    // getCommunityDocuments - đổi đầu method:
    public CommunityPageResponseDTO getCommunityDocuments(String keyword, Long subjectId, Double minRating, String sortBy, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = DEFAULT_COMMUNITY_PAGE_SIZE;
        // Trần kích thước trang: mọi tài liệu PUBLIC đều được nạp và map (mapper còn đếm
        // favorite/download/rating + tải bình luận cho TỪNG tài liệu), nên ?size=1000000 là một
        // request rẻ tiền kéo theo hàng nghìn truy vấn. Client thật chỉ dùng 12.
        if (size > MAX_COMMUNITY_PAGE_SIZE) size = MAX_COMMUNITY_PAGE_SIZE;

        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        List<DocDocument> all = docDocumentRepository.findCommunityDocuments(DocumentVisibility.PUBLIC, kw, subjectId);
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
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("error.document.toggleVisibilityForbidden");
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
        if (result.isMetadataMismatch()) {
            doc.setModerationReason((result.getReason() == null ? "" : result.getReason())
                    + " | Lưu ý metadata: " + result.getMetadataMismatchReason());
            notifyMetadataMismatch(doc, result.getMetadataMismatchReason());
        }

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
        LocalDateTime metadataCheckedAt = LocalDateTime.now();
        docDocumentRepository.stampMetadataCheck(savedDocument.getId(),
                result.isMetadataMismatch() ? "LECH" : "KHOP", metadataCheckedAt);

        // stampMetadataCheck là @Modifying(clearAutomatically = true): nó XOÁ SẠCH persistence
        // context, nên savedDocument (và proxy lazy `user` của nó) thành detached ngay giữa
        // transaction này. Chạm vào doc.getUser() sau đó — DocumentMapper làm đúng thế để lấy
        // ownerName — sẽ ném LazyInitializationException và endpoint trả 500. Nạp lại thực thể
        // để mọi thứ phía dưới làm việc với bản còn gắn với session.
        DocDocument screenedDocument = docDocumentRepository.findById(savedDocument.getId())
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
        notifyAdminsDocumentScreened(screenedDocument, result.getDecision().name());

        return documentMapper.toResponseDTO(screenedDocument);
    }

    private void notifyAdminsDocumentScreened(DocDocument document, String outcome) {
        try {
            String message = "Tài liệu \"" + document.getTitle()
                    + "\" đã được sàng lọc: " + outcome;
            authUserRepository.findByRole_RoleNameAndStatus(RoleNames.ADMIN, UserStatus.ACTIVE)
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

    private void notifyMetadataMismatch(DocDocument document, String reason) {
        try {
            notificationService.createDocumentNotification(document.getUser().getId(), NotificationType.METADATA_MISMATCH,
                    "Tiêu đề/môn học của '" + document.getTitle() + "' có vẻ chưa khớp nội dung — bạn nên chỉnh lại.", document.getId());
            authUserRepository.findByRole_RoleNameAndStatus(RoleNames.ADMIN, UserStatus.ACTIVE).forEach(admin ->
                    notificationService.createDocumentNotification(admin.getId(), NotificationType.METADATA_MISMATCH,
                            "Metadata tài liệu '" + document.getTitle() + "' có dấu hiệu chưa khớp nội dung: " + reason, document.getId()));
        } catch (Exception exception) {
            log.warn("Không thể gửi thông báo metadata mismatch cho document {}; publish vẫn tiếp tục.", document.getId(), exception);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponseDTO getDocumentById(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
        // Đã xoá mềm (đang ở thùng rác) -> coi như không tồn tại với MỌI người, kể cả chủ sở
        // hữu, giống DocumentAccessPortImpl.isAvailableTo(). Kiểm tra trước cả check quyền để
        // không lộ ra rằng id đó từng tồn tại (403 "cấm" khác hẳn 404 "không có").
        if (doc.getDeletedAt() != null) {
            throw new ResourceNotFoundException("error.document.notFound");
        }
        // Chi tiết tài liệu cũng là một kênh lấy NỘI DUNG (mô tả, bình luận, tên tệp) nên phải
        // áp đúng luật truy cập của getFileForPreview()/getFileByDocumentId(): chủ sở hữu /
        // PUBLIC / được chia sẻ. Thiếu chốt này thì bất kỳ user đã đăng nhập nào cũng đọc được
        // metadata tài liệu PRIVATE của người khác chỉ bằng cách đoán id.
        AuthUser currentUser = getCurrentUser();
        Long currentUserId = currentUser.getId();
        boolean isOwner = doc.getUser().getId().equals(currentUserId);
        boolean isPublic = doc.getVisibility() == DocumentVisibility.PUBLIC;
        boolean isShared = documentShareService.hasShareAccess(id, currentUserId);
        if (!isOwner && !isPublic && !isShared && !isAdmin(currentUser)) {
            throw new ForbiddenException("error.document.viewForbidden");
        }
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
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
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
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
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
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
        doc.setDeletedAt(null);
        docDocumentRepository.save(doc);
    }
}
