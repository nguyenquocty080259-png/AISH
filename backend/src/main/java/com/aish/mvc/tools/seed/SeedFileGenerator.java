package com.aish.mvc.tools.seed;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Standalone seed utility — SINH file mẫu (PDF/DOCX/TXT/PPTX) với nội dung học thuật thật
 * vào backend/src/main/resources/seed-docs/, kèm manifest JSON để bước seed-DB kế tiếp map
 * classpath resource -> Document.
 *
 * KHÔNG phải Spring bean, KHÔNG có @Component/@SpringBootApplication — không boot ApplicationContext,
 * không cần Postgres chạy, không bao giờ tự chạy khi `mvn spring-boot:run` khởi động app bình
 * thường. Đây là lựa chọn "standalone script" trong 2 lựa chọn đề bài đưa ra (thay vì
 * CommandLineRunner có flag), vì việc sinh file không có lý do gì phải phụ thuộc DB/Spring context.
 *
 * CÁCH CHẠY (từ thư mục backend/):
 *   mvn exec:java -Dexec.mainClass=com.aish.mvc.tools.seed.SeedFileGenerator
 *
 * TÙY CHỌN (system property, đều có default hợp lý):
 *   -Dapp.seed.outputDir=<path>   thư mục xuất file; mặc định src/main/resources/seed-docs khi
 *                                 chạy từ module backend, hoặc backend/src/main/resources/seed-docs
 *                                 khi chạy từ root monorepo
 *   -Dapp.seed.font=<path.ttf>    font Unicode nhúng vào PDF, mặc định tự dò C:/Windows/Fonts/arial.ttf
 *   -Dapp.seed.fontBold=<path.ttf>
 *
 * IDEMPOTENT: chạy lại sẽ tự xoá đúng các file mà lần chạy trước đã sinh ra (dựa vào manifest
 * cũ) trước khi sinh lại — không cộng dồn file rác qua nhiều lần chạy, và không đụng tới file
 * upload thật của người dùng vì chỉ xoá đúng những gì có tên nằm trong manifest.
 */
public final class SeedFileGenerator {

    // Nhân đôi trọng số PDF vì đây là định dạng phổ biến nhất trong thực tế và là định dạng
    // DUY NHẤT cần test citation có page thật — còn lại DOCX/TXT/PPTX test nhánh page=null.
    private static final List<String> FORMAT_CYCLE = List.of("PDF", "DOCX", "PPTX", "TXT", "PDF");
    private static final List<String> CANONICAL_SUBJECTS = List.of(
            "Toán", "Ngữ văn", "Tiếng Anh", "Vật lý", "Hóa học",
            "Sinh học", "Lịch sử", "Địa lý", "Tin học", "GDCD");
    private static final int DOCUMENTS_PER_SUBJECT = 5;

    private static final String MANIFEST_FILE_NAME = "seed-manifest.json";

    public static void main(String[] args) throws Exception {
        Path uploadsDir = Path.of(System.getProperty("app.seed.outputDir", defaultOutputDirectory()));
        Files.createDirectories(uploadsDir);
        Path manifestPath = uploadsDir.resolve(MANIFEST_FILE_NAME);

        cleanupPreviousRun(uploadsDir, manifestPath);

        List<Topic> bank = ThptContentBank.all();
        List<Topic> topics = new ArrayList<>(CANONICAL_SUBJECTS.size() * DOCUMENTS_PER_SUBJECT);
        for (String subject : CANONICAL_SUBJECTS) {
            List<Topic> selected = bank.stream()
                    .filter(topic -> subject.equals(topic.subjectOrDomain()))
                    .limit(DOCUMENTS_PER_SUBJECT)
                    .toList();
            if (selected.size() != DOCUMENTS_PER_SUBJECT) {
                throw new IllegalStateException("Môn " + subject + " phải có ít nhất "
                        + DOCUMENTS_PER_SUBJECT + " chủ đề, hiện có " + selected.size());
            }
            topics.addAll(selected);
        }

        System.out.println("Ngân hàng nội dung: " + topics.size() + " chủ đề thật (không lặp).");

        List<SeedManifestEntry> manifest = new ArrayList<>();
        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            String primaryFormat = FORMAT_CYCLE.get(i % FORMAT_CYCLE.size());
            manifest.add(generateOne(topic, primaryFormat, uploadsDir));
            System.out.printf("[%3d/%d] (%s) %-10s %s%n", i + 1, topics.size(), primaryFormat, topic.subjectOrDomain(), topic.title());

        }

        writeManifest(manifestPath, manifest);
        printSummary(manifest, manifestPath);
    }

    private static SeedManifestEntry generateOne(Topic topic, String format, Path uploadsDir) throws IOException {
        String slug = SlugUtil.slugify(topic.title());
        String extension = extensionFor(format);
        String originalFileName = slug + "." + extension;
        // Khớp CHÍNH XÁC pattern FileStorageServiceImpl.storeFile(): {uuid}_{originalFilename}
        String storedFileName = UUID.randomUUID() + "_" + originalFileName;
        File outFile = uploadsDir.resolve(storedFileName).toFile();

        switch (format) {
            case "PDF" -> PdfTextRenderer.render(outFile, topic.title(), topic.paragraphs());
            case "DOCX" -> DocxRenderer.render(outFile, topic.title(), topic.paragraphs());
            case "PPTX" -> PptxRenderer.render(outFile, topic.title(), topic.paragraphs());
            case "TXT" -> TxtRenderer.render(outFile, topic.title(), topic.paragraphs());
            default -> throw new IllegalStateException("Định dạng không hỗ trợ: " + format);
        }

        return new SeedManifestEntry(storedFileName, originalFileName, mimeFor(format), format,
                topic.level(), topic.track(), topic.subjectOrDomain(), topic.title(), outFile.length());
    }

    private static String defaultOutputDirectory() {
        return Files.exists(Path.of("pom.xml"))
                ? "src/main/resources/seed-docs"
                : "backend/src/main/resources/seed-docs";
    }

    private static String extensionFor(String format) {
        return switch (format) {
            case "PDF" -> "pdf";
            case "DOCX" -> "docx";
            case "PPTX" -> "pptx";
            case "TXT" -> "txt";
            default -> throw new IllegalStateException("Định dạng không hỗ trợ: " + format);
        };
    }

    private static String mimeFor(String format) {
        return switch (format) {
            case "PDF" -> "application/pdf";
            case "DOCX" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "PPTX" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "TXT" -> "text/plain";
            default -> throw new IllegalStateException("Định dạng không hỗ trợ: " + format);
        };
    }

    private static void cleanupPreviousRun(Path uploadsDir, Path manifestPath) throws IOException {
        if (!Files.exists(manifestPath)) {
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        SeedManifestEntry[] previous = mapper.readValue(manifestPath.toFile(), SeedManifestEntry[].class);
        int deleted = 0;
        for (SeedManifestEntry entry : previous) {
            if (Files.deleteIfExists(uploadsDir.resolve(entry.storedFileName()))) {
                deleted++;
            }
        }
        Files.deleteIfExists(manifestPath);
        System.out.println("Đã dọn " + deleted + " file từ lần chạy seed trước (idempotent re-run).");
    }

    private static void writeManifest(Path manifestPath, List<SeedManifestEntry> manifest) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // Writer + UTF_8 explicit để manifest tiếng Việt không phụ thuộc charset mặc định của máy.
        try (Writer writer = Files.newBufferedWriter(manifestPath, StandardCharsets.UTF_8)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(writer, manifest);
        }
    }

    private static void printSummary(List<SeedManifestEntry> manifest, Path manifestPath) {
        System.out.println();
        System.out.println("=== TỔNG KẾT ===");
        System.out.println("Tổng số file đã sinh: " + manifest.size());

        Map<String, Long> byFormat = new TreeMap<>();
        Map<String, Long> byLevel = new TreeMap<>();
        Map<String, Long> bySubject = new TreeMap<>();
        for (SeedManifestEntry e : manifest) {
            byFormat.merge(e.format(), 1L, Long::sum);
            byLevel.merge(e.level(), 1L, Long::sum);
            bySubject.merge(e.level() + " / " + e.subjectOrDomain(), 1L, Long::sum);
        }

        System.out.println("Theo định dạng: " + byFormat);
        System.out.println("Theo cấp độ: " + byLevel);
        System.out.println("Theo môn/ngành:");
        bySubject.forEach((k, v) -> System.out.println("  " + k + ": " + v));
        System.out.println("Manifest: " + manifestPath.toAbsolutePath());
    }
}
