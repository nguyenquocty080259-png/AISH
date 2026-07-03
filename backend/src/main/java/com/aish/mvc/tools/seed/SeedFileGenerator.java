package com.aish.mvc.tools.seed;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Standalone seed utility — SINH file mẫu (PDF/DOCX/TXT/PPTX) với nội dung học thuật thật
 * vào backend/uploads/, kèm manifest JSON để bước seed-DB kế tiếp map file -> Document.
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
 *   -Dapp.seed.uploadDir=<path>   thư mục xuất file, mặc định "uploads" (khớp app.upload.dir)
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
    private static final List<String> FORMAT_CYCLE = List.of("PDF", "PDF", "DOCX", "TXT", "PPTX");

    // Cứ mỗi N chủ đề thì chủ đề đó được render THÊM 1 định dạng khác nữa (cùng nội dung thật,
    // khác định dạng file — hợp lý về mặt thực tế: 1 bài giảng có thể có cả bản slide lẫn bản
    // ghi chú). Đây là cách hợp lệ để tăng tổng số file lên gần mốc 100-150 mà không phải bịa
    // thêm nội dung giả — xem báo cáo cuối cùng để biết vì sao KHÔNG đặt N=1 (sẽ nhân đôi hết).
    private static final int EXTRA_VARIANT_EVERY_N = 3;

    private static final String MANIFEST_FILE_NAME = "seed-manifest.json";

    public static void main(String[] args) throws Exception {
        Path uploadsDir = Path.of(System.getProperty("app.seed.uploadDir", "uploads"));
        Files.createDirectories(uploadsDir);
        Path manifestPath = uploadsDir.resolve(MANIFEST_FILE_NAME);

        cleanupPreviousRun(uploadsDir, manifestPath);

        List<Topic> topics = new ArrayList<>();
        topics.addAll(ThptContentBank.all());
        topics.addAll(UniversityContentBank.all());

        System.out.println("Ngân hàng nội dung: " + topics.size() + " chủ đề thật (không lặp).");

        List<SeedManifestEntry> manifest = new ArrayList<>();
        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            String primaryFormat = FORMAT_CYCLE.get(i % FORMAT_CYCLE.size());
            manifest.add(generateOne(topic, primaryFormat, uploadsDir));
            System.out.printf("[%3d/%d] (%s) %-10s %s%n", i + 1, topics.size(), primaryFormat, topic.subjectOrDomain(), topic.title());

            if (i % EXTRA_VARIANT_EVERY_N == 0) {
                String extraFormat = pickExtraFormat(i, primaryFormat);
                manifest.add(generateOne(topic, extraFormat, uploadsDir));
                System.out.printf("        + variant (%s)%n", extraFormat);
            }
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

    private static String pickExtraFormat(int index, String primary) {
        String candidate = FORMAT_CYCLE.get((index + 2) % FORMAT_CYCLE.size());
        if (candidate.equals(primary)) {
            candidate = FORMAT_CYCLE.get((index + 3) % FORMAT_CYCLE.size());
        }
        return candidate;
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
        mapper.writerWithDefaultPrettyPrinter().writeValue(manifestPath.toFile(), manifest);
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
