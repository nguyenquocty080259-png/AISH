package com.aish.mvc.tools.seed;

import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthRole;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.entity.doc.Subject;
import com.aish.mvc.entity.doc.Tag;
import com.aish.mvc.entity.enums.AuthProviders;
import com.aish.mvc.entity.enums.DocumentStatus;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.IngestStatus;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.entity.enums.UserStatus;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.auth.AuthRoleRepository;
import com.aish.mvc.repository.auth.AuthUserRepository;
import com.aish.mvc.repository.doc.CollectionItemRepository;
import com.aish.mvc.repository.doc.CommentRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.DownloadRepository;
import com.aish.mvc.repository.doc.FavoriteRepository;
import com.aish.mvc.repository.doc.ModerationAppealRepository;
import com.aish.mvc.repository.doc.RatingRepository;
import com.aish.mvc.repository.doc.SubjectRepository;
import com.aish.mvc.repository.doc.TagRepository;
import com.aish.mvc.repository.doc.ViewHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Standalone-DẠNG-2 seed utility (bước 2 trong chuỗi seed): đọc backend/uploads/seed-manifest.json
 * (do SeedFileGenerator sinh ở bước 1) và tạo User + DocDocument + DocFile + Subject tương ứng
 * trong Postgres. KHÔNG chạy ingest/embedding — mọi document tạo ra có ingestStatus=NOT_INGESTED.
 *
 * Khác với SeedFileGenerator (bước 1, standalone main() vì không cần DB), bước này BẮT BUỘC cần
 * JPA repository + PasswordEncoder + kết nối Postgres thật, nên chọn nhánh còn lại đề bài cho phép:
 * CommandLineRunner có cờ chặn — @ConditionalOnProperty khiến bean này KHÔNG được tạo (và do đó
 * không bao giờ chạy) trừ khi property dưới đây = true. Mặc định app.spring boot run bình thường
 * sẽ KHÔNG kích hoạt runner này.
 *
 * CÁCH CHẠY (từ thư mục backend/, cần Postgres đang chạy đúng cấu hình application.properties):
 *   mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Dapp.seed.db.enabled=true
 * hoặc set trong application.properties tạm thời rồi chạy bình thường.
 *
 * IDEMPOTENT: chạy lại sẽ tự dọn user/document được tạo bởi lần seed DB trước (nhận diện qua
 * domain email @seed.aish.local) trước khi seed lại — không cộng dồn, không đụng dữ liệu thật.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.db.enabled", havingValue = "true")
public class DbSeedRunner implements CommandLineRunner {

    private static final String SEED_EMAIL_DOMAIN = "@seed.aish.local";
    // Mật khẩu cố định cho MỌI user seed (admin + 100 customer) — chỉ dùng để demo/test cục bộ.
    private static final String SEED_PASSWORD = "Seed@12345";

    private static final int TOTAL_CUSTOMERS = 100;
    private static final int UNIVERSITY_OWNER_COUNT = 6; // trong khoảng 5-7 theo yêu cầu
    private static final int THPT_OWNER_COUNT = 9;        // 6+9=15 chủ sở hữu, trong khoảng 10-20

    // Cứ mỗi PUBLIC_EVERY_N tài liệu thì có 1 tài liệu PUBLIC, còn lại PRIVATE (mặc định).
    private static final int PUBLIC_EVERY_N = 3;

    private static final String[] SEED_TAG_NAMES = {
            "đề cương", "bài tập", "lý thuyết", "ôn thi", "slide bài giảng", "ghi chú",
            "đề thi thử", "tài liệu tham khảo", "thực hành", "case study",
            "machine learning", "ielts", "kỹ năng mềm", "chuyên đề", "tổng hợp"
    };

    private final AuthUserRepository authUserRepository;
    private final AuthAccountRepository authAccountRepository;
    private final AuthRoleRepository authRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DocDocumentRepository docDocumentRepository;
    private final SubjectRepository subjectRepository;
    private final TagRepository tagRepository;
    private final FavoriteRepository favoriteRepository;
    private final RatingRepository ratingRepository;
    private final CommentRepository commentRepository;
    private final DownloadRepository downloadRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final CollectionItemRepository collectionItemRepository;
    private final ModerationAppealRepository moderationAppealRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // email theo userId — tránh phải query lại DB để in báo cáo, vì mình vừa mới sinh ra chúng.
    private final Map<Long, String> emailByUserId = new LinkedHashMap<>();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        cleanupPreviousSeed();

        AuthRole adminRole = findOrCreateRole("ADMIN");
        AuthRole userRole = findOrCreateRole("USER");

        AuthUser admin = createUser("Quản Trị Viên Seed", "admin" + SEED_EMAIL_DOMAIN, adminRole);

        List<AuthUser> customers = new ArrayList<>(TOTAL_CUSTOMERS);
        for (int i = 0; i < TOTAL_CUSTOMERS; i++) {
            String fullName = VietnameseNameBank.fullNameFor(i);
            String email = VietnameseNameBank.emailFor(i, fullName, SEED_EMAIL_DOMAIN);
            customers.add(createUser(fullName, email, userRole));
        }

        List<AuthUser> universityOwners = customers.subList(0, UNIVERSITY_OWNER_COUNT);
        List<AuthUser> thptOwners = customers.subList(UNIVERSITY_OWNER_COUNT, UNIVERSITY_OWNER_COUNT + THPT_OWNER_COUNT);

        List<SeedManifestEntry> manifest = readManifest();
        List<SeedManifestEntry> universityEntries = manifest.stream()
                .filter(e -> "UNIVERSITY".equals(e.level())).toList();
        List<SeedManifestEntry> thptEntries = manifest.stream()
                .filter(e -> "THPT".equals(e.level())).toList();

        Map<String, Subject> subjectCache = new LinkedHashMap<>();
        List<DocDocument> created = new ArrayList<>(manifest.size());
        int[] publicCounter = {0};

        seedDocumentGroup(universityEntries, universityOwners, subjectCache, created, publicCounter);
        seedDocumentGroup(thptEntries, thptOwners, subjectCache, created, publicCounter);

        List<Tag> tags = seedTags();

        printSummary(admin, customers, universityOwners, thptOwners, created, subjectCache, tags);
    }

    // ---------------------------------------------------------------- cleanup (idempotent re-run)

    private void cleanupPreviousSeed() {
        List<AuthAccount> seedAccounts = authAccountRepository.findByIdentifierEndingWithIgnoreCase(SEED_EMAIL_DOMAIN);
        if (seedAccounts.isEmpty()) {
            System.out.println("Không có dữ liệu seed DB cũ để dọn.");
            return;
        }

        List<Long> userIds = seedAccounts.stream().map(a -> a.getUser().getId()).distinct().toList();
        List<DocDocument> seedDocs = docDocumentRepository.findByUser_IdIn(userIds);

        // Dọn các bảng có thể tham chiếu tới document seed (kể cả do NGƯỜI DÙNG THẬT tương tác
        // với 1 document PUBLIC do seed tạo ra, vd. rate/comment/favorite/thêm vào collection) —
        // bắt buộc phải xoá trước, nếu không sẽ vướng FK khi xoá document.
        for (DocDocument doc : seedDocs) {
            Long docId = doc.getId();
            favoriteRepository.deleteByDocumentId(docId);
            ratingRepository.deleteByDocumentId(docId);
            commentRepository.deleteByDocumentId(docId);
            downloadRepository.deleteByDocumentId(docId);
            viewHistoryRepository.deleteByDocumentId(docId);
            collectionItemRepository.deleteByDocumentId(docId);
            moderationAppealRepository.deleteByDocumentId(docId);
        }
        // Xoá DocDocument cascade luôn DocFile (cascade=ALL, orphanRemoval) và các dòng
        // document_subjects (bảng nối phía chủ — Hibernate tự dọn khi xoá entity chủ),
        // KHÔNG đụng tới Subject (Subject có thể đang được document KHÁC, không phải seed, dùng).
        docDocumentRepository.deleteAll(seedDocs);

        authAccountRepository.deleteAll(seedAccounts);
        authUserRepository.deleteAllById(userIds);

        System.out.println("Đã dọn seed DB cũ: " + userIds.size() + " user, " + seedDocs.size() + " document.");
    }

    // ---------------------------------------------------------------------------- user creation

    private AuthUser createUser(String fullName, String email, AuthRole role) {
        AuthUser user = new AuthUser();
        user.setFullName(fullName);
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(role);
        authUserRepository.save(user);

        AuthAccount account = new AuthAccount();
        account.setUser(user);
        account.setProvider(AuthProviders.LOCAL);
        account.setIdentifier(email);
        account.setPasswordHash(passwordEncoder.encode(SEED_PASSWORD));
        // Bỏ qua luồng OTP thật (chỉ dùng khi signup qua API) — seed set thẳng verified=true
        // để user demo đăng nhập được ngay.
        account.setIsVerified(true);
        account.setIsPrimary(true);
        authAccountRepository.save(account);

        emailByUserId.put(user.getId(), email);
        return user;
    }

    private AuthRole findOrCreateRole(String roleName) {
        return authRoleRepository.findByRoleName(roleName).orElseGet(() -> {
            AuthRole role = new AuthRole();
            role.setRoleName(roleName);
            return authRoleRepository.save(role);
        });
    }

    // ------------------------------------------------------------------------ document creation

    private void seedDocumentGroup(List<SeedManifestEntry> entries, List<AuthUser> owners,
                                    Map<String, Subject> subjectCache, List<DocDocument> out, int[] publicCounter) {
        for (int i = 0; i < entries.size(); i++) {
            SeedManifestEntry entry = entries.get(i);
            AuthUser owner = owners.get(i % owners.size());
            boolean isPublic = publicCounter[0] % PUBLIC_EVERY_N == 0;
            publicCounter[0]++;

            Subject subject = subjectCache.computeIfAbsent(entry.subjectOrDomain(), this::findOrCreateSubject);

            DocDocument doc = new DocDocument();
            doc.setTitle(entry.title());
            doc.setDescription(buildDescription(entry));
            doc.setStatus(DocumentStatus.COMPLETED);
            doc.setVisibility(isPublic ? DocumentVisibility.PUBLIC : DocumentVisibility.PRIVATE);
            // Khớp đúng invariant thật của app: PUBLIC luôn đi kèm moderationStatus=APPROVED
            // (xem DocumentServiceImpl.toggleVisibility) — seed không gọi AI moderation thật.
            doc.setModerationStatus(isPublic ? ModerationStatus.APPROVED : ModerationStatus.NOT_REQUIRED);
            doc.setIngestStatus(IngestStatus.NOT_INGESTED);
            doc.setUser(owner);
            doc.getSubjects().add(subject);

            DocFile file = new DocFile();
            file.setFileName(entry.originalFileName());
            // Local storage: fileUrl chỉ là TÊN FILE trên đĩa (không http prefix) — khớp đúng
            // FileStorageServiceImpl/resolveResource (branch "không bắt đầu bằng http").
            file.setFileUrl(entry.storedFileName());
            file.setFileType(entry.fileType());
            file.setFileSize(entry.fileSizeBytes());
            file.setResourceType("local");
            doc.addFile(file);

            docDocumentRepository.save(doc);
            out.add(doc);
        }
    }

    private String buildDescription(SeedManifestEntry entry) {
        String levelLabel = "THPT".equals(entry.level()) ? "Tài liệu THPT" : "Tài liệu Đại học";
        return levelLabel + " - môn/ngành " + entry.subjectOrDomain()
                + ". Sinh tự động từ seed manifest (dữ liệu demo).";
    }

    // Subject không có field "owner" trong schema hiện tại — "Admin-owned" ở đây chỉ mang tính
    // quy ước nghiệp vụ (giống việc tạo Subject thật luôn đi qua API hasRole("ADMIN")), KHÔNG
    // có cột nào lưu lại điều đó trên bảng subjects.
    private Subject findOrCreateSubject(String name) {
        return subjectRepository.findByName(name).orElseGet(() -> {
            Subject subject = Subject.builder()
                    .name(name)
                    .description("Môn/ngành: " + name + " (tạo tự động khi seed dữ liệu demo).")
                    .build();
            return subjectRepository.save(subject);
        });
    }

    // Tag KHÔNG có bất kỳ quan hệ nào với DocDocument trong schema hiện tại (đã xác nhận: không
    // có join table, không có FK) — nên chỉ tạo sẵn các Tag row để bảng "tags" không rỗng, KHÔNG
    // thể gắn vào document nào (xem báo cáo cuối cùng để biết vì sao và cần quyết định gì).
    private List<Tag> seedTags() {
        List<Tag> tags = new ArrayList<>(SEED_TAG_NAMES.length);
        for (String name : SEED_TAG_NAMES) {
            Tag tag = tagRepository.findByName(name).orElseGet(() -> {
                Tag t = Tag.builder().name(name).build();
                return tagRepository.save(t);
            });
            tags.add(tag);
        }
        return tags;
    }

    // --------------------------------------------------------------------------------- manifest

    private List<SeedManifestEntry> readManifest() throws Exception {
        Path manifestPath = Path.of(uploadDir).resolve("seed-manifest.json");
        if (!Files.exists(manifestPath)) {
            throw new IllegalStateException("Không tìm thấy manifest tại " + manifestPath.toAbsolutePath()
                    + " — hãy chạy SeedFileGenerator (bước 1) trước khi chạy DbSeedRunner.");
        }
        SeedManifestEntry[] entries = objectMapper.readValue(manifestPath.toFile(), SeedManifestEntry[].class);
        return List.of(entries);
    }

    // ----------------------------------------------------------------------------------- report

    private void printSummary(AuthUser admin, List<AuthUser> customers, List<AuthUser> universityOwners,
                               List<AuthUser> thptOwners, List<DocDocument> created,
                               Map<String, Subject> subjectCache, List<Tag> tags) {
        long publicCount = created.stream().filter(d -> d.getVisibility() == DocumentVisibility.PUBLIC).count();
        long privateCount = created.size() - publicCount;

        System.out.println();
        System.out.println("=== TỔNG KẾT SEED DB ===");
        System.out.println("Admin: " + emailByUserId.get(admin.getId()));
        System.out.println("Customer users: " + customers.size());
        System.out.println("Mật khẩu chung cho MỌI user seed: " + SEED_PASSWORD);
        System.out.println("Document đã tạo: " + created.size() + " (PUBLIC=" + publicCount + ", PRIVATE=" + privateCount + ")");
        System.out.println("Chủ sở hữu ĐH (" + universityOwners.size() + "): " + ownerEmails(universityOwners));
        System.out.println("Chủ sở hữu THPT (" + thptOwners.size() + "): " + ownerEmails(thptOwners));
        System.out.println("Subject đã find-or-create: " + subjectCache.size() + " -> " + subjectCache.keySet());
        System.out.println("Tag đã find-or-create (KHÔNG gắn được vào document, xem báo cáo): " + tags.size());
    }

    private String ownerEmails(List<AuthUser> owners) {
        List<String> emails = new ArrayList<>();
        for (AuthUser owner : owners) {
            emails.add(emailByUserId.get(owner.getId()));
        }
        return String.join(", ", emails);
    }
}
