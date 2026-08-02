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

/**
 * TRÁI TIM của luồng tài liệu — nơi thực hiện mọi nghiệp vụ chính: tải tài liệu lên (kèm các
 * chốt chặn tuổi/loại tệp/dung lượng/quota), sửa thông tin, thùng rác (xoá mềm - khôi phục - xoá
 * vĩnh viễn), xin công khai qua kiểm duyệt AI + Admin (toggleVisibility), lấy file để xem
 * trước/tải về, trang Cộng đồng, và các thao tác dành riêng cho Admin.
 *
 * <p>Hai nguyên tắc xuyên suốt: (1) mọi thao tác đều tính theo user đang đăng nhập lấy từ token,
 * KHÔNG nhận userId do client gửi lên; (2) mọi lối lấy nội dung (chi tiết, xem trước, tải về)
 * đều áp cùng một luật quyền: chủ sở hữu / tài liệu PUBLIC / được chia sẻ / Admin.
 *
 * <p>Việc dựng dữ liệu trả về FE giao cho {@link DocumentMapper}; lưu file vật lý giao cho
 * {@code FileStorageService} (đĩa máy chủ) và {@code CloudinaryService} (đám mây).
 */
@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);

    // Phân trang trang Cộng đồng: mặc định khớp với lưới 12 thẻ của FE, trần chặn request cố ý
    // xin cả kho tài liệu trong một lần gọi.
    static final int DEFAULT_COMMUNITY_PAGE_SIZE = 12;
    static final int MAX_COMMUNITY_PAGE_SIZE = 50;

    // Giá trị của DocDocument.aiScreenOutcome — kết quả pre-screen kèm theo lần chờ Admin duyệt.
    static final String AI_SCREEN_PASS = "PASS";
    static final String AI_SCREEN_FLAG = "FLAG";

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

    // Lấy user đang đăng nhập: đọc email từ token trong SecurityContext rồi tra ra bản ghi user.
    // Dùng ở hầu hết các method để biết "ai đang thao tác" — nhờ vậy không cần client gửi userId
    // (client gửi thì có thể giả mạo thành người khác).
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
    /**
     * Danh sách tài liệu cho trang "Tài liệu của tôi".
     *
     * <p>Đầu vào: không có (tự lấy user đang đăng nhập). Trả về: danh sách DTO tài liệu của
     * CHÍNH user đó và chưa nằm trong thùng rác.
     */
    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> getAllDocuments() {
        Long uid = getCurrentUser().getId();
        // Điều kiện truy vấn: deletedAt IS NULL (chưa xoá) VÀ user_id = mình.
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

        // Tính tuổi = khoảng cách từ ngày sinh tới hôm nay, lấy phần số năm tròn.
        int age = Period.between(dob, LocalDate.now()).getYears();
        // Tuổi tối thiểu đọc từ bảng system_settings (Admin chỉnh được), mặc định 16.
        int minAge = systemSettingService.getInt(
                SystemSettingService.MIN_UPLOAD_AGE_KEY, SystemSettingService.MIN_UPLOAD_AGE_DEFAULT);
        // Chưa đủ tuổi -> chặn upload ngay, trả lỗi 400.
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
            // Giới hạn dung lượng 1 tệp khi lưu trên đĩa máy chủ, đọc từ system_settings.
            long maxLocal = systemSettingService.getLong(
                    SystemSettingService.MAX_FILE_LOCAL_BYTES_KEY, SystemSettingService.MAX_FILE_LOCAL_BYTES_DEFAULT);
            // Tệp to hơn mức cho phép -> chặn, tránh một người up file khổng lồ làm đầy ổ đĩa.
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
            // Cộng tổng dung lượng các file LOCAL user này đang chiếm (truy vấn SUM trên doc_files).
            long usedLocal = docFileRepository.sumLocalFileSizeByUserId(userId);
            long quotaLocal = systemSettingService.getLong(
                    SystemSettingService.QUOTA_LOCAL_BYTES_KEY, SystemSettingService.QUOTA_LOCAL_BYTES_DEFAULT);
            // Đã dùng + tệp mới mà vượt quota -> chặn (mỗi user chỉ được dùng tối đa từng đó dung lượng).
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
        doc.setUser(getCurrentUser()); // chủ sở hữu = người đang đăng nhập
        doc.setStatus(DocumentStatus.COMPLETED);
        // DEC-006: Private by default — không kiểm duyệt lúc upload, chỉ khi user chủ động
        // chuyển sang PUBLIC (toggleVisibility) mới bắt buộc AI pre-screen (DEC-035).
        doc.setVisibility(DocumentVisibility.PRIVATE);
        // NOT_REQUIRED = "chưa cần kiểm duyệt", vì tài liệu đang riêng tư, chưa ai ngoài chủ thấy.
        doc.setModerationStatus(ModerationStatus.NOT_REQUIRED);
        doc.setSubjects(subjects);

        // Ghi bản ghi tài liệu xuống database (bảng doc_documents: tiêu đề, mô tả, chủ sở hữu,
        // trạng thái, chế độ hiển thị + bảng nối tài liệu-môn học). Chưa có file đính kèm.
        return docDocumentRepository.save(doc);
    }

    /** Tải lên và lưu trên ĐĨA máy chủ — gọi lại {@link #uploadDocument} với nơi lưu LOCAL. */
    @Override
    @Transactional
    public DocumentResponseDTO uploadDocumentToServer(String title, String description, java.util.List<Long> subjectIds, MultipartFile file) {
        return uploadDocument(title, description, subjectIds, file, StorageTarget.LOCAL);
    }

    /** Tải lên và lưu trên ĐÁM MÂY Cloudinary — gọi lại {@link #uploadDocument} với nơi lưu CLOUD. */
    @Override
    @Transactional
    public DocumentResponseDTO uploadDocumentToCloud(String title, String description, java.util.List<Long> subjectIds, MultipartFile file) {
        return uploadDocument(title, description, subjectIds, file, StorageTarget.CLOUD);
    }

    // Upload hợp nhất: storage = "LOCAL" | "CLOUD" | "BOTH".
    // BOTH: lưu cả local lẫn Cloudinary — file local được add TRƯỚC để mọi nơi đọc
    // (preview/download/ingest AI) ưu tiên bản local, không phụ thuộc Cloudinary.
    /**
     * TẢI TÀI LIỆU LÊN — method quan trọng nhất của luồng tài liệu.
     *
     * <p>Đầu vào: tiêu đề, mô tả, danh sách id môn học, file người dùng chọn, và nơi lưu
     * ("LOCAL" = đĩa máy chủ, "CLOUD" = Cloudinary, "BOTH" = cả hai). Trả về: DTO tài liệu vừa
     * tạo để FE hiển thị ngay.
     *
     * <p>Các bước:
     * <br>1. Kiểm tra người dùng đủ tuổi tối thiểu.
     * <br>2. Kiểm tra nơi lưu hợp lệ.
     * <br>3. Kiểm tra loại tệp được phép (chống đổi đuôi lừa hệ thống).
     * <br>4. Kiểm tra dung lượng một tệp và tổng quota của user.
     * <br>5. Tạo bản ghi tài liệu trong database (mặc định PRIVATE - riêng tư).
     * <br>6. Sinh ảnh thumbnail (không bắt buộc thành công).
     * <br>7. Ghi file thật lên đĩa và/hoặc lên Cloudinary, mỗi bản lưu một dòng trong doc_files.
     *
     * <p>Cả method nằm trong một @Transactional: bước nào lỗi thì các bản ghi đã tạo trong DB
     * bị huỷ hết, không để lại tài liệu dở dang.
     */
    @Override
    @Transactional
    public DocumentResponseDTO uploadDocument(String title, String description, java.util.List<Long> subjectIds, MultipartFile file, String storage) {
        // B1: chưa đủ tuổi (hoặc chưa khai ngày sinh) -> dừng luôn, chưa đụng gì tới file.
        enforceMinUploadAge();

        // B2: chuẩn hoá tham số nơi lưu (viết hoa, bỏ khoảng trắng); không truyền gì thì mặc định LOCAL.
        String target = storage == null ? StorageTarget.LOCAL : storage.trim().toUpperCase();
        if (!StorageTarget.ALL.contains(target)) {
            throw new IllegalArgumentException("storage phải là LOCAL, CLOUD hoặc BOTH (nhận được: " + storage + ")");
        }

        // B3: chốt chặn loại tệp (allowlist đuôi + đối chiếu content-type thật, chống đổi đuôi).
        // Chống đổi đuôi lừa: đổi tên virus.exe thành baitap.pdf vẫn bị phát hiện vì hệ thống đọc
        // "vân tay" nội dung file chứ không tin phần mở rộng.
        // Đọc allowlist tươi từ DB mỗi lần nên admin đổi là ăn liền, không cần restart.
        uploadFileTypeService.validate(file);

        // B4: chặn tệp quá to, và chặn nếu tổng dung lượng user đã dùng vượt quota được cấp.
        enforceUploadSizeLimit(file.getSize(), target);
        enforceUploadQuota(file.getSize(), target, getCurrentUser().getId());

        // B5: tạo bản ghi tài liệu trong DB trước (chưa gắn file), tài liệu mới luôn PRIVATE.
        DocDocument savedDoc = buildDocument(title, description, subjectIds);

        // B6: Thumbnail sinh 1 lần cho cả 2 bản (best-effort, null nếu định dạng không hỗ trợ).
        String thumbnailUrl = thumbnailService.createThumbnail(file);

        // B7a: nhánh lưu trên ĐĨA máy chủ (áp dụng cho cả LOCAL lẫn BOTH).
        if (StorageTarget.LOCAL.equals(target) || StorageTarget.BOTH.equals(target)) {
            // Ghi nội dung file thật vào thư mục uploads; trả về tên file đã đổi để không trùng.
            String storedFileName = fileStorageService.storeFile(file);
            // Mô tả file vừa lưu: tên gốc để hiển thị, tên đã lưu để tìm lại file trên đĩa,
            // loại/kích thước để kiểm tra và thống kê dung lượng.
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
            // Ghi bản ghi file xuống database (bảng doc_files): lưu tên file, đường dẫn, loại,
            // dung lượng, thumbnail và id tài liệu cha.
            docFileRepository.save(localFile);
        }

        // B7b: nhánh lưu trên ĐÁM MÂY Cloudinary (áp dụng cho cả CLOUD lẫn BOTH).
        if (StorageTarget.CLOUD.equals(target) || StorageTarget.BOTH.equals(target)) {
            // Đẩy file lên Cloudinary; nhận về URL truy cập + publicId (dùng để xoá sau này).
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
            // Ghi bản ghi file xuống database (bảng doc_files) — dòng riêng cho bản trên đám mây,
            // nên chọn BOTH sẽ có 2 dòng doc_files cho cùng 1 tài liệu.
            docFileRepository.save(cloudFile);
        }

        // Đổi entity sang DTO để FE hiển thị ngay tài liệu vừa tạo.
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

    /**
     * Sửa thông tin tài liệu (tiêu đề, mô tả, môn học) — CHỈ chủ sở hữu được sửa.
     *
     * <p>Đầu vào: id tài liệu + các trường muốn sửa (để null nghĩa là giữ nguyên). Trả về: DTO
     * tài liệu sau khi sửa.
     *
     * <p>Các bước: (1) tìm tài liệu, (2) tài liệu trong thùng rác thì coi như không tồn tại,
     * (3) chặn nếu người gọi không phải chủ sở hữu, (4) kiểm duyệt tiêu đề mới, (5) lưu lại.
     */
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

        // Tiêu đề mới phải qua kiểm duyệt tên: đủ dài, không toàn số/ký tự lặp, không chứa từ cấm.
        String validatedTitle = title == null ? null : namingModerationService.validate(title);
        applyDocumentUpdate(doc, validatedTitle, description, subjectIds);

        // Ghi thay đổi xuống database (bảng doc_documents + bảng nối tài liệu-môn học).
        return documentMapper.toResponseDTO(docDocumentRepository.save(doc));
    }

    /**
     * Danh sách tài liệu user đã bấm "Yêu thích" (trang Yêu thích).
     *
     * <p>Các bước: (1) lấy id các tài liệu user đã thích từ bảng favorites, (2) nạp các tài liệu
     * đó nhưng bỏ qua tài liệu đã nằm trong thùng rác.
     */
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

    /**
     * Số liệu dung lượng của user cho thanh "đã dùng / tổng quota" và để FE tự chặn trước khi
     * gửi file quá lớn. Trả về: đã dùng + quota + giới hạn 1 tệp, tách riêng cho LOCAL và CLOUD.
     */
    @Override
    @Transactional(readOnly = true)
    public StorageUsageDTO getStorageUsage() {
        Long uid = getCurrentUser().getId();
        // Cộng tổng dung lượng file của user, tách theo nơi lưu (kể cả file trong thùng rác,
        // vì bytes vẫn còn chiếm chỗ cho tới khi xoá vĩnh viễn).
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

    /**
     * Đưa tài liệu vào THÙNG RÁC (xoá mềm) — chỉ chủ sở hữu. Đầu vào: id tài liệu.
     * Không xoá dữ liệu thật, chỉ đánh dấu thời điểm xoá để có thể khôi phục.
     */
    @Override
    @Transactional
    public void deleteDocument(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("error.document.deleteForbidden");
        }
        // XOÁ MỀM: không xoá dòng nào cả, chỉ ghi thời điểm xoá vào cột deleted_at. Mọi truy vấn
        // khác đều lọc "deleted_at IS NULL" nên tài liệu biến mất khỏi giao diện nhưng vẫn khôi
        // phục được. File trên đĩa/Cloudinary vẫn còn nguyên.
        doc.setDeletedAt(LocalDateTime.now());
        docDocumentRepository.save(doc); // cập nhật bản ghi trong bảng doc_documents
    }

    /** Danh sách tài liệu trong THÙNG RÁC của user đang đăng nhập (deleted_at khác null). */
    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> getDeletedDocuments() {
        Long uid = getCurrentUser().getId();
        // CHỈ trả về rác của user đang đăng nhập (trước đây bị lỗi trả về rác của TẤT CẢ user)
        return docDocumentRepository.findByDeletedAtIsNotNullAndUser_Id(uid).stream()
                .map(documentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * KHÔI PHỤC tài liệu từ thùng rác — chỉ chủ sở hữu. Đầu vào: id tài liệu.
     * Chỉ cần xoá dấu thời điểm xoá là tài liệu hiện lại như cũ.
     */
    @Override
    @Transactional
    public void restoreDocument(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("error.document.restoreForbidden");
        }
        doc.setDeletedAt(null); // xoá dấu "đã xoá" -> tài liệu quay lại danh sách bình thường
        docDocumentRepository.save(doc); // cập nhật bản ghi trong bảng doc_documents
    }

    /**
     * XOÁ VĨNH VIỄN tài liệu — chỉ chủ sở hữu. Đây là thao tác KHÔNG khôi phục được.
     *
     * <p>Đầu vào: id tài liệu. Không trả về gì.
     *
     * <p>Các bước: (1) kiểm tra quyền, (2) xoá file vật lý trên đĩa/Cloudinary + ảnh thumbnail,
     * (3) xoá mọi dữ liệu liên quan ở các bảng khác (bình luận, đánh giá, yêu thích, lượt tải,
     * bộ sưu tập, embedding AI, kháng cáo, lịch sử xem, chia sẻ), (4) xoá hẳn dòng tài liệu.
     * Phải xoá dữ liệu liên quan TRƯỚC, nếu không sẽ vướng ràng buộc khoá ngoại của database.
     */
    @Override
    @Transactional
    public void permanentDeleteDocument(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
        if (!doc.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("error.document.permanentDeleteForbidden");
        }

        // B2: xoá file thật. Tài liệu lưu BOTH có 2 dòng file nên vòng lặp chạy 2 lần.
        if (doc.getFiles() != null) {
            for (DocFile f : doc.getFiles()) {
                if (DocFile.RESOURCE_TYPE_LOCAL.equals(f.getResourceType())) {
                    fileStorageService.deleteFile(f.getFileUrl()); // xoá file trong thư mục uploads
                } else if (f.getPublicId() != null) {
                    // Xoá file trên Cloudinary. Lỗi mạng thì bỏ qua để không chặn việc xoá dưới DB.
                    try { cloudinaryService.delete(f.getPublicId(), f.getResourceType()); }
                    catch (Exception ignored) {}
                }
                thumbnailService.deleteThumbnail(f.getThumbnailUrl()); // xoá luôn ảnh xem trước
            }
        }

        // B3: dọn sạch mọi dữ liệu ở các bảng đang tham chiếu tới tài liệu này.
        commentRepository.deleteByDocumentId(id);
        ratingRepository.deleteByDocumentId(id);
        favoriteRepository.deleteByDocumentId(id);
        downloadRepository.deleteByDocumentId(id);
        collectionItemRepository.deleteByDocumentId(id);
        docEmbeddingRepository.deleteByDocument_Id(id);
        moderationAppealRepository.deleteByDocumentId(id);
        viewHistoryRepository.deleteByDocumentId(id);
        documentShareRepository.deleteByDocumentId(id);
        // Hội thoại AI thì KHÔNG xoá, chỉ gỡ tham chiếu tới tài liệu (giữ lại lịch sử chat).
        aiConversationRepository.clearDocumentReference(id);

        // B4: xoá hẳn dòng tài liệu khỏi bảng doc_documents (và các dòng doc_files theo cascade).
        docDocumentRepository.delete(doc);
    }

    /**
     * Lấy file để TẢI VỀ.
     *
     * <p>Đầu vào: id tài liệu. Trả về: bản ghi file (DocFile) để controller mở nội dung gửi cho
     * người dùng.
     *
     * <p>Các bước: (1) tìm tài liệu, (2) kiểm tra quyền — chỉ chủ sở hữu, tài liệu PUBLIC,
     * người được chia sẻ hoặc Admin mới được tải, (3) chọn file chính (ưu tiên bản trên đĩa).
     */
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

    /**
     * Lấy file để XEM TRƯỚC ngay trên trình duyệt (không tính là lượt tải). Dùng chung cho
     * /preview, /thumbnail và /preview-text.
     *
     * <p>Đầu vào: id tài liệu. Trả về: bản ghi file chính. Luật quyền giống hệt tải về.
     */
    @Override
    @Transactional(readOnly = true)
    public DocFile getFileForPreview(Long id) {
        DocDocument doc = docDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.document.notFound"));
        AuthUser currentUser = getCurrentUser();
        Long currentUserId = currentUser.getId();
        // 3 điều kiện cho phép xem: là chủ sở hữu, tài liệu đang công khai, hoặc được chia sẻ.
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
    /**
     * Dữ liệu cho TRANG CỘNG ĐỒNG — nơi mọi người xem tài liệu công khai của nhau.
     *
     * <p>Đầu vào: từ khoá tìm kiếm, môn học, điểm đánh giá tối thiểu, kiểu sắp xếp, số trang và
     * số tài liệu mỗi trang. Trả về: một trang kết quả kèm tổng số tài liệu và tổng số trang.
     *
     * <p>Các bước: (1) làm sạch tham số phân trang, (2) truy vấn tài liệu PUBLIC theo từ
     * khoá/môn học, (3) đổi sang DTO, (4) lọc theo điểm trung bình, (5) sắp xếp, (6) cắt ra
     * đúng trang người dùng yêu cầu.
     */
    public CommunityPageResponseDTO getCommunityDocuments(String keyword, Long subjectId, Double minRating, String sortBy, int page, int size) {
        // B1: kẹp lại tham số phân trang cho hợp lệ (trang âm -> 0, size <= 0 -> mặc định).
        if (page < 0) page = 0;
        if (size <= 0) size = DEFAULT_COMMUNITY_PAGE_SIZE;
        // Trần kích thước trang: mọi tài liệu PUBLIC đều được nạp và map (mapper còn đếm
        // favorite/download/rating + tải bình luận cho TỪNG tài liệu), nên ?size=1000000 là một
        // request rẻ tiền kéo theo hàng nghìn truy vấn. Client thật chỉ dùng 12.
        if (size > MAX_COMMUNITY_PAGE_SIZE) size = MAX_COMMUNITY_PAGE_SIZE;

        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        // B2: chỉ lấy tài liệu đang PUBLIC — tài liệu riêng tư không bao giờ lọt ra trang này.
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

        // B6: cắt danh sách đã sắp xếp thành đúng 1 trang. Math.min để không vượt quá cỡ danh sách.
        long totalItems = mapped.size();
        int totalPages = (int) Math.ceil(totalItems / (double) size);
        int fromIndex = Math.min(page * size, mapped.size());
        int toIndex = Math.min(fromIndex + size, mapped.size());
        List<DocumentResponseDTO> pageItems = mapped.subList(fromIndex, toIndex);

        return new CommunityPageResponseDTO(pageItems, page, size, totalItems, totalPages);
    }

    /**
     * Đảo chế độ hiển thị. PUBLIC -> PRIVATE là thao tác an toàn, làm ngay. Chiều ngược lại
     * KHÔNG bao giờ tự động công khai nữa: mọi yêu cầu công khai đều dừng ở ADMIN_PENDING và
     * tài liệu ở lại PRIVATE cho tới khi Admin duyệt (AdminServiceImpl.approveDocumentReview)
     * hoặc từ chối (removeDocumentReview). AI pre-screen chỉ còn là gợi ý cho Admin, kết quả
     * PASS/FLAG lưu ở aiScreenOutcome.
     *
     * <p>Nói ngắn gọn: đây là luồng XIN CÔNG KHAI TÀI LIỆU. Đầu vào là id tài liệu, trả về DTO
     * tài liệu sau khi đổi để FE hiện trạng thái mới.
     *
     * <p>Các bước:
     * <br>1. Tìm tài liệu, chỉ chủ sở hữu được đổi.
     * <br>2. Nếu đang PUBLIC -> chuyển về PRIVATE ngay (ẩn bớt luôn an toàn), kết thúc.
     * <br>3. Nếu xin công khai: xoá kết quả duyệt cũ của Admin (đây là yêu cầu mới).
     * <br>4. Quét từ khoá cấm trong nội dung (rẻ, không tốn tiền gọi AI). Trúng -> gắn cờ FLAG,
     *        đưa vào hàng chờ Admin, kết thúc.
     * <br>5. Không trúng từ khoá -> gọi AI kiểm duyệt, ghi kết quả PASS/FLAG.
     * <br>6. Dù AI PASS hay FLAG, tài liệu vẫn Ở LẠI PRIVATE và chờ Admin duyệt cuối cùng.
     * <br>7. Gửi thông báo cho Admin và cho chủ tài liệu.
     */
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

        // -> PUBLIC: reset lớp admin review trước mọi loại pre-screen — đây là một yêu cầu
        // công khai MỚI, quyết định cũ của Admin (nếu có) không còn hiệu lực.
        doc.setAdminReviewedAt(null);
        doc.setAdminReviewedBy(null);

        // Keyword DB là lớp rẻ nhất. Hit thì KHÔNG gọi AI (tiết kiệm), nhưng cũng KHÔNG từ chối
        // cứng nữa: đi cùng đường với AI FLAG — vào hàng chờ Admin, Admin mới là người quyết
        // định cuối. Lý do keyword được ghi lại để Admin biết vì sao tài liệu bị gắn cờ.
        // B4: đối chiếu nội dung tài liệu với danh sách từ khoá cấm do Admin quản lý.
        if (documentContentKeywordService.matches(doc)) {
            log.info("Document {} matched a DOCUMENT_CONTENT keyword; skipping AI moderation screen", documentId);
            doc.setVisibility(DocumentVisibility.PRIVATE); // vẫn riêng tư, chưa ai ngoài chủ thấy
            // ADMIN_PENDING = "đang xếp hàng chờ Admin xem xét", hiện ở tab "Cần xem xét" bên Admin.
            doc.setModerationStatus(ModerationStatus.ADMIN_PENDING);
            // FLAG = "bị gắn cờ nghi ngờ" — chỉ là gợi ý cho Admin, không phải quyết định từ chối.
            doc.setAiScreenOutcome(AI_SCREEN_FLAG);
            doc.setModerationReason("Nội dung tài liệu kích hoạt quy tắc từ khóa không phù hợp.");
            // Ghi trạng thái kiểm duyệt xuống database (bảng doc_documents).
            DocDocument flagged = docDocumentRepository.save(doc);
            notifyAdminsDocumentScreened(flagged, "PENDING_FLAGGED_BY_CONTENT_KEYWORD"); // báo Admin có việc
            notifyOwnerDocPending(flagged, true); // báo chủ tài liệu là đang chờ duyệt
            return documentMapper.toResponseDTO(flagged);
        }

        // B5: Không trúng keyword: tiếp tục AI pre-screen hiện có (DEC-035).
        // Gọi AI đọc nội dung tài liệu và cho nhận xét có phù hợp để công khai hay không.
        ModerationResultDTO result = aiModerationService.screen(documentId);
        doc.setModerationReason(result.getReason()); // lý do AI đưa ra, để Admin và chủ tài liệu đọc
        if (result.isMetadataMismatch()) {
            doc.setModerationReason((result.getReason() == null ? "" : result.getReason())
                    + " | Lưu ý metadata: " + result.getMetadataMismatchReason());
            notifyMetadataMismatch(doc, result.getMetadataMismatchReason());
        }

        // Dù AI PASS hay FLAG, tài liệu đều ở lại PRIVATE và vào hàng chờ Admin duyệt cuối.
        // Kết quả AI chỉ được ghi vào aiScreenOutcome để Admin (và FE) biết ngữ cảnh.
        // B6: AI trả PASS nghĩa là "không thấy vấn đề", khác PASS thì coi như bị gắn cờ.
        boolean aiFlagged = result.getDecision() != ModerationDecision.PASS;
        doc.setVisibility(DocumentVisibility.PRIVATE); // vẫn chưa công khai — chờ Admin quyết định
        doc.setModerationStatus(ModerationStatus.ADMIN_PENDING); // vào hàng chờ duyệt của Admin
        // PASS = AI thấy ổn, FLAG = AI thấy nghi ngờ. Chỉ để Admin tham khảo khi bấm duyệt.
        doc.setAiScreenOutcome(aiFlagged ? AI_SCREEN_FLAG : AI_SCREEN_PASS);

        // Ghi kết quả kiểm duyệt xuống database (bảng doc_documents).
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
        // B7: gửi thông báo trong ứng dụng — Admin biết có tài liệu cần duyệt, chủ tài liệu biết
        // yêu cầu công khai đã được ghi nhận.
        notifyAdminsDocumentScreened(screenedDocument, result.getDecision().name());
        notifyOwnerDocPending(screenedDocument, aiFlagged);

        return documentMapper.toResponseDTO(screenedDocument);
    }

    // Gửi thông báo cho TẤT CẢ Admin đang hoạt động: có tài liệu vừa qua sàng lọc, cần xem xét.
    // Bọc try/catch: gửi thông báo lỗi thì chỉ ghi log, KHÔNG làm hỏng luồng kiểm duyệt.
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

    // Yêu cầu công khai đã được ghi nhận và đang chờ Admin duyệt cuối — chưa có quyết định nào
    // cả, nên dùng DOCUMENT_SCREENED (thông báo trung tính) thay vì DOC_APPROVED/DOC_REJECTED;
    // hai loại đó dành riêng cho quyết định thật của Admin (xem AdminServiceImpl).
    private void notifyOwnerDocPending(DocDocument document, boolean aiFlagged) {
        try {
            String message = aiFlagged
                    ? "Tài liệu \"" + document.getTitle() + "\" của bạn còn vài chỗ chưa phù hợp, "
                        + "đang chờ Admin xem xét."
                    : "Tài liệu \"" + document.getTitle() + "\" của bạn đã qua kiểm duyệt tự động "
                        + "và đang chờ Admin xem xét.";
            notificationService.createDocumentNotification(document.getUser().getId(),
                    NotificationType.DOCUMENT_SCREENED, message, document.getId());
        } catch (Exception exception) {
            log.error("Không thể gửi thông báo chờ duyệt tài liệu id={} cho chủ sở hữu.", document.getId(), exception);
        }
    }

    // Báo cho chủ tài liệu và Admin khi AI thấy tiêu đề/môn học có vẻ không khớp nội dung file
    // (vd. đặt tên "Toán 12" nhưng nội dung là bài Văn). Chỉ nhắc nhở, không chặn công khai.
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

    /**
     * Chi tiết một tài liệu cho trang xem tài liệu.
     *
     * <p>Đầu vào: id tài liệu. Trả về: DTO đầy đủ (thông tin, môn học, số lượt thích/tải, điểm
     * đánh giá, danh sách bình luận).
     *
     * <p>Các bước: (1) tìm tài liệu, (2) trong thùng rác thì báo không tồn tại, (3) kiểm tra
     * quyền xem (chủ sở hữu / PUBLIC / được chia sẻ / Admin), (4) đổi sang DTO.
     */
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

    /**
     * DANH SÁCH TÀI LIỆU CHO TRANG QUẢN TRỊ — Admin thấy được TOÀN BỘ tài liệu của mọi người,
     * bất kể riêng tư hay công khai.
     *
     * <p>Đầu vào: các bộ lọc (chế độ hiển thị, cờ "chỉ lấy hàng chờ duyệt", từ khoá, trạng thái
     * kiểm duyệt, đã gỡ hay chưa) + thông tin phân trang. Trả về: một trang các dòng tóm tắt
     * tài liệu để đổ vào bảng.
     *
     * <p>Các bước: (1) nếu bật cờ "cần xem xét" thì chỉ lấy tài liệu đang ADMIN_PENDING và bỏ
     * qua mọi bộ lọc khác (hàng chờ phải luôn hiện đủ); (2) ngược lại thì tìm kiếm theo các bộ
     * lọc; (3) đổi mỗi tài liệu thành một dòng tóm tắt.
     *
     * <p>Không kiểm tra quyền ở đây vì đường dẫn /api/admin/** đã yêu cầu vai trò ADMIN.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AdminDocumentSummaryDTO> getAllDocumentsForAdmin(
            DocumentVisibility visibility,
            boolean needsReview,
            String keyword,
            ModerationStatus moderationStatus,
            Boolean removed,
            Pageable pageable) {

        Page<DocDocument> page;

        if (needsReview) {

            // Tab "Cần xem xét": đúng các tài liệu đang chờ Admin quyết định. Các bộ lọc còn lại
            // bị bỏ qua ở nhánh này — hàng chờ luôn phải hiện đầy đủ.
            page = docDocumentRepository
                    .findByModerationStatusAndDeletedAtIsNull(ModerationStatus.ADMIN_PENDING, pageable);

        } else {

            // Từ khoá rỗng = không lọc (chuẩn hoá tại đây để query chỉ cần so null).
            String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
            page = docDocumentRepository.searchForAdminList(
                    normalizedKeyword, visibility, moderationStatus, removed, pageable);

        }
        // Đổi mỗi tài liệu thành 1 dòng gọn cho bảng admin (không kèm bình luận/số liệu tương tác
        // để bảng nhẹ và nhanh).
        return page.map(document -> {

            // Tài liệu chưa có file nào thì hiển thị dấu "-" ở cột nơi lưu.
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
                    document.getDeletedAt(),
                    document.getAiScreenOutcome()
            );
        });
    }

    /**
     * Admin GỠ tài liệu vi phạm (xoá mềm — vẫn khôi phục được). Đầu vào: id tài liệu.
     * Khác {@link #deleteDocument} ở chỗ không cần là chủ sở hữu.
     */
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

    /**
     * Admin SỬA thông tin tài liệu (vd. sửa tiêu đề/môn học đặt sai). Đầu vào: id + các trường
     * muốn sửa. Trả về: DTO sau khi sửa. Không cần là chủ sở hữu.
     */
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

    /**
     * Admin KHÔI PHỤC tài liệu đã gỡ (vd. gỡ nhầm, hoặc chủ tài liệu kháng cáo thành công).
     * Đầu vào: id tài liệu. Không cần là chủ sở hữu.
     */
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
