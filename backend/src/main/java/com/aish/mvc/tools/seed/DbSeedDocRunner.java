package com.aish.mvc.tools.seed;

import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.entity.doc.Subject;
import com.aish.mvc.entity.enums.DocumentStatus;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.IngestStatus;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.DocFileRepository;
import com.aish.mvc.repository.doc.SubjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Seed khoảng 50 tài liệu THPT đóng gói trong classpath seed-docs/. Runner chỉ chạy khi bật
 * app.seed.db.enabled=true, dùng SYSTEM admin có sẵn làm owner và không gọi ingest/embedding.
 */
@Component
@RequiredArgsConstructor
@Order(10)
@ConditionalOnProperty(name = "app.seed.db.enabled", havingValue = "true")
public class DbSeedDocRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DbSeedDocRunner.class);
    private static final String SYSTEM_ADMIN_EMAIL = "admin@aish.com";
    private static final String SEED_RESOURCE_ROOT = "seed-docs/";
    private static final String MANIFEST_RESOURCE = SEED_RESOURCE_ROOT + "seed-manifest.json";
    private static final Set<String> CANONICAL_SUBJECTS = Set.of(
            "Toán", "Ngữ văn", "Tiếng Anh", "Vật lý", "Hóa học",
            "Sinh học", "Lịch sử", "Địa lý", "Tin học", "GDCD");

    private final AuthAccountRepository authAccountRepository;
    private final DocDocumentRepository docDocumentRepository;
    private final DocFileRepository docFileRepository;
    private final SubjectRepository subjectRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        AuthUser systemAdmin = authAccountRepository.findByIdentifier(SYSTEM_ADMIN_EMAIL)
                .map(AuthAccount::getUser)
                .orElse(null);
        if (systemAdmin == null) {
            log.error("Bỏ qua document seed: không tìm thấy SYSTEM admin {}. "
                    + "Hãy bảo đảm config.DbSeedRunner đã chạy trước.", SYSTEM_ADMIN_EMAIL);
            return;
        }

        Map<String, Subject> subjects = seedCanonicalSubjects();
        List<SeedManifestEntry> manifest = readManifest();
        Path targetDirectory = Path.of(uploadDir);
        Files.createDirectories(targetDirectory);

        int created = 0;
        int skipped = 0;
        for (int index = 0; index < manifest.size(); index++) {
            SeedManifestEntry entry = manifest.get(index);
            validateEntry(entry);

            if (docFileRepository.existsByFileUrl(entry.storedFileName())) {
                skipped++;
                continue;
            }

            Path targetFile = safeTarget(targetDirectory, entry.storedFileName());
            copyClasspathFileIfMissing(entry.storedFileName(), targetFile);
            createDocument(systemAdmin, subjects.get(entry.subjectOrDomain().trim()), entry, targetFile, index);
            created++;
        }

        log.info("Document seed hoàn tất: tạo mới {}, bỏ qua {} tài liệu đã tồn tại, tổng manifest {}.",
                created, skipped, manifest.size());
    }

    private Map<String, Subject> seedCanonicalSubjects() {
        Map<String, Subject> subjects = new LinkedHashMap<>();
        for (String canonicalName : List.of(
                "Toán", "Ngữ văn", "Tiếng Anh", "Vật lý", "Hóa học",
                "Sinh học", "Lịch sử", "Địa lý", "Tin học", "GDCD")) {
            Subject subject = subjectRepository.findByName(canonicalName).orElseGet(() ->
                    subjectRepository.save(Subject.builder()
                            .name(canonicalName)
                            .description("Môn học THPT: " + canonicalName)
                            .build()));
            subjects.put(canonicalName, subject);
        }
        return subjects;
    }

    private void createDocument(AuthUser owner, Subject subject, SeedManifestEntry entry,
                                Path targetFile, int index) throws IOException {
        boolean isPublic = index % 3 != 2;

        DocDocument document = new DocDocument();
        document.setTitle(entry.title().trim());
        document.setDescription("Tài liệu THPT môn " + entry.subjectOrDomain().trim()
                + ", được chuẩn bị làm dữ liệu demo HiveMind.");
        document.setStatus(DocumentStatus.COMPLETED);
        document.setVisibility(isPublic ? DocumentVisibility.PUBLIC : DocumentVisibility.PRIVATE);
        document.setModerationStatus(isPublic
                ? ModerationStatus.APPROVED
                : ModerationStatus.NOT_REQUIRED);
        document.setIngestStatus(IngestStatus.NOT_INGESTED);
        document.setUser(owner);
        document.getSubjects().add(subject);

        DocFile file = new DocFile();
        file.setFileName(entry.originalFileName());
        file.setFileUrl(entry.storedFileName());
        file.setFileType(entry.fileType());
        file.setFileSize(Files.size(targetFile));
        file.setResourceType("local");
        document.addFile(file);

        docDocumentRepository.save(document);
    }

    private List<SeedManifestEntry> readManifest() throws IOException {
        try (InputStream input = resourceStream(MANIFEST_RESOURCE)) {
            SeedManifestEntry[] entries = objectMapper.readValue(input, SeedManifestEntry[].class);
            return List.of(entries);
        }
    }

    private void copyClasspathFileIfMissing(String storedFileName, Path targetFile) throws IOException {
        if (Files.exists(targetFile)) {
            return;
        }
        try (InputStream input = resourceStream(SEED_RESOURCE_ROOT + storedFileName)) {
            Files.copy(input, targetFile);
        }
    }

    private InputStream resourceStream(String resourceName) {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        if (input == null) {
            throw new IllegalStateException("Không tìm thấy classpath resource: " + resourceName
                    + ". Hãy chạy SeedFileGenerator.main() rồi rebuild ứng dụng.");
        }
        return input;
    }

    private static Path safeTarget(Path targetDirectory, String storedFileName) {
        Path normalizedDirectory = targetDirectory.toAbsolutePath().normalize();
        Path target = normalizedDirectory.resolve(storedFileName).normalize();
        if (!target.getParent().equals(normalizedDirectory)) {
            throw new IllegalArgumentException("Tên seed file không an toàn: " + storedFileName);
        }
        return target;
    }

    private static void validateEntry(SeedManifestEntry entry) {
        if (entry.storedFileName() == null || entry.storedFileName().isBlank()
                || entry.originalFileName() == null || entry.originalFileName().isBlank()
                || entry.fileType() == null || entry.fileType().isBlank()
                || entry.title() == null || entry.title().isBlank()
                || entry.subjectOrDomain() == null) {
            throw new IllegalArgumentException("Seed manifest có entry thiếu field bắt buộc: " + entry);
        }
        String subject = entry.subjectOrDomain().trim();
        if (!CANONICAL_SUBJECTS.contains(subject)) {
            throw new IllegalArgumentException("Môn trong seed manifest không canonical: " + subject);
        }
    }
}
