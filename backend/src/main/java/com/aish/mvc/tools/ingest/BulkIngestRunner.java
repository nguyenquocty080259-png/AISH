package com.aish.mvc.tools.ingest;

import com.aish.mvc.dto.ai.IngestResponseDTO;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.entity.enums.IngestStatus;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.service.doc.DocEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Bulk ingest utility (bước 3, sau DbSeedRunner): quét doc_documents có ingestStatus=NOT_INGESTED
 * và gọi THẲNG DocEmbeddingService.ingest() có sẵn (KHÔNG tự chunk/embed lại) cho từng tài liệu.
 *
 * Vì sao cần runner riêng thay vì gọi API /api/ai/ingest/{id} thủ công 120 lần: Gemini free tier
 * rate-limit rất chặt, cần batch nhỏ + throttle + resumable qua nhiều lần chạy — CommandLineRunner
 * có cờ chặn (giống DbSeedRunner) là chỗ tự nhiên để làm việc này.
 *
 * CÁCH CHẠY (từ thư mục backend/, cần Postgres + doc đã seed từ DbSeedRunner):
 *   mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dapp.ingest.seed.enabled=true -Dapp.ingest.seed.max-docs=5 -Dapp.ingest.seed.delay-ms=4000"
 *
 * RESUMABLE: chỉ lấy tài liệu NOT_INGESTED (ORDER BY id ASC) giới hạn app.ingest.seed.max-docs —
 * ingest() tự set INGESTED khi thành công nên lần chạy sau tự động bỏ qua tài liệu đã xong, KHÔNG
 * cộng dồn/KHÔNG embed lại. Nếu bị rate-limit dừng giữa batch, chạy lại lệnh trên là tiếp tục đúng chỗ.
 *
 * THROTTLE: retry-with-backoff cho lỗi 429/5xx TỪNG CHUNK đã có sẵn trong DocEmbeddingServiceImpl
 * (không đụng vào, không viết lại RAG) — runner này CHỈ thêm delay CẤU HÌNH ĐƯỢC giữa các LƯỢT
 * ingest từng TÀI LIỆU (ranh giới tự nhiên không phải sửa service có sẵn) để dàn đều tải lên Gemini.
 *
 * LỖI TỪNG TÀI LIỆU: bắt riêng lẻ mỗi lần gọi ingest() — 1 tài liệu lỗi vĩnh viễn (hết retry) chỉ
 * được log + giữ nguyên NOT_INGESTED, KHÔNG dừng cả batch.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.ingest.seed.enabled", havingValue = "true")
public class BulkIngestRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BulkIngestRunner.class);

    // Impersonate đúng admin do DbSeedRunner tạo — ingest() yêu cầu owner-hoặc-ADMIN, mà tài liệu
    // seed thuộc về nhiều owner khác nhau nên ADMIN là cách duy nhất để 1 batch job xử lý hết.
    private static final String SEED_ADMIN_EMAIL = "admin@seed.aish.local";

    private final DocDocumentRepository docDocumentRepository;
    private final DocEmbeddingService docEmbeddingService;

    @Value("${app.ingest.seed.max-docs:5}")
    private int maxDocs;

    // Delay giữa 2 tài liệu liên tiếp — KHÔNG phải giữa từng chunk (xem javadoc lớp).
    @Value("${app.ingest.seed.delay-ms:4000}")
    private long delayMs;

    @Override
    public void run(String... args) {
        List<DocDocument> targets = docDocumentRepository.findNotIngestedBatch(
                IngestStatus.NOT_INGESTED, PageRequest.of(0, maxDocs));

        if (targets.isEmpty()) {
            System.out.println("Không còn tài liệu NOT_INGESTED nào — bulk ingest không có gì để làm.");
            return;
        }

        System.out.println("Bulk ingest: xử lý " + targets.size() + " tài liệu (giới hạn "
                + maxDocs + "/lần chạy, delay " + delayMs + "ms/tài liệu)...");

        impersonateAdmin();
        List<String> lines = new ArrayList<>(targets.size());
        int success = 0, failed = 0, totalChunks = 0;

        try {
            for (int i = 0; i < targets.size(); i++) {
                DocDocument doc = targets.get(i);
                String format = formatOf(doc);

                try {
                    IngestResponseDTO result = docEmbeddingService.ingest(doc.getId());
                    if ("INGESTED".equals(result.getStatus())) {
                        success++;
                        totalChunks += result.getChunkCount();
                        lines.add(String.format("[OK]   doc=%d format=%s chunks=%d — %s",
                                doc.getId(), format, result.getChunkCount(), result.getMessage()));
                    }
                    else {
                        // NO_FILE / UNSUPPORTED_FORMAT / FILE_ERROR / EMPTY — không phải lỗi
                        // tạm thời, ingest() đã tự xử lý trạng thái, không cần retry ở đây.
                        failed++;
                        lines.add(String.format("[SKIP] doc=%d format=%s status=%s — %s",
                                doc.getId(), format, result.getStatus(), result.getMessage()));
                    }
                }
                catch (RuntimeException ex) {
                    // Hết retry (429/5xx) hoặc lỗi vĩnh viễn khác — giữ nguyên NOT_INGESTED,
                    // log lại và tiếp tục tài liệu kế tiếp, KHÔNG dừng cả batch.
                    failed++;
                    log.error("Bulk ingest: tài liệu {} thất bại vĩnh viễn: {}", doc.getId(), ex.getMessage());
                    lines.add(String.format("[FAIL] doc=%d format=%s — %s", doc.getId(), format, ex.getMessage()));
                }

                if (i < targets.size() - 1) {
                    sleep(delayMs);
                }
            }
        }
        finally {
            SecurityContextHolder.clearContext();
        }

        System.out.println();
        System.out.println("=== TỔNG KẾT BULK INGEST ===");
        lines.forEach(System.out::println);
        System.out.println("Thành công: " + success + "/" + targets.size());
        System.out.println("Thất bại/skip: " + failed + "/" + targets.size());
        System.out.println("Tổng số chunk đã tạo: " + totalChunks);
    }

    private void impersonateAdmin() {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        var auth = new UsernamePasswordAuthenticationToken(SEED_ADMIN_EMAIL, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private static String formatOf(DocDocument doc) {
        if (doc.getFiles() == null || doc.getFiles().isEmpty()) return "NONE";
        DocFile file = doc.getFiles().getFirst();
        String type = file.getFileType();
        String name = file.getFileName() != null ? file.getFileName().toLowerCase() : "";
        if (type != null && type.contains("pdf") || name.endsWith(".pdf")) return "PDF";
        if (name.endsWith(".docx")) return "DOCX";
        if (name.endsWith(".pptx")) return "PPTX";
        if (name.endsWith(".txt")) return "TXT";
        return type != null ? type : "UNKNOWN";
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Bulk ingest bị gián đoạn trong lúc chờ throttle delay", ie);
        }
    }
}
