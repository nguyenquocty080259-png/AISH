package com.aish.mvc.service.doc.impl;

import com.aish.mvc.config.HydratableSimpleVectorStore;
import com.aish.mvc.dto.ai.IngestResponseDTO;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.DocEmbedding;
import com.aish.mvc.entity.doc.DocFile;
import com.aish.mvc.entity.enums.IngestStatus;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.DocEmbeddingRepository;
import com.aish.mvc.service.doc.DocEmbeddingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.errors.ApiException;
import com.google.genai.errors.GenAiIOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStoreContent;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocEmbeddingServiceImpl implements DocEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(DocEmbeddingServiceImpl.class);

    // Retry chỉ áp dụng cho lỗi tạm thời (429 rate-limit, timeout mạng, 5xx) khi gọi
    // Gemini embed — không retry lỗi 4xx khác (input xấu...), vì thử lại không giúp gì.
    private static final int MAX_EMBED_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 1000L;

    // Ngưỡng "đủ nội dung để ingest" sau khi Tika trích xuất — dưới ngưỡng này coi như file
    // rỗng/không phải văn bản thật (ảnh quét không OCR, file lỗi...), không đáng để embed.
    // 20 ký tự non-whitespace là ngưỡng nhỏ có chủ đích: đủ để loại rác thật sự, không loại
    // nhầm file hợp lệ nhưng ngắn (vd. slide chỉ có tiêu đề).
    private static final int MIN_USEFUL_CONTENT_CHARS = 20;

    // Blocklist: định dạng vô nghĩa khi đọc như text — mọi định dạng KHÁC (kể cả lạ/hiếm)
    // đều được thử qua Tika, đúng tinh thần "đọc được càng nhiều định dạng càng tốt".
    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            // Ảnh
            ".png", ".jpg", ".jpeg", ".gif", ".webp", ".bmp", ".tiff", ".tif", ".svg",
            // Video
            ".mp4", ".mov", ".avi", ".mkv", ".webm", ".wmv", ".flv", ".m4v",
            // Audio
            ".mp3", ".wav", ".ogg", ".flac", ".aac", ".m4a", ".wma",
            // Nén
            ".zip", ".rar", ".7z", ".tar", ".gz",
            // Thực thi / nhị phân
            ".exe", ".dll", ".so", ".bin"
    );

    private final DocDocumentRepository docDocumentRepository;
    private final DocEmbeddingRepository docEmbeddingRepository;
    private final AuthAccountRepository authAccountRepository;
    private final EmbeddingModel embeddingModel;
    private final HydratableSimpleVectorStore vectorStore;

    // Instance riêng, không lấy bean của Spring — Boot 4 autoconfigure ở đây là
    // tools.jackson.databind.json.JsonMapper (Jackson 3), không phải ObjectMapper cổ điển.
    // Dùng để (de)serialize float[] <-> JSON, không cần chia sẻ config với tầng web.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    @Transactional
    public IngestResponseDTO ingest(Long documentId) {
        DocDocument doc = docDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Tài liệu không tồn tại!"));

        Long currentUserId = getCurrentUserId();
        boolean isOwner = doc.getUser().getId().equals(currentUserId);
        if (!isOwner && !isAdmin()) {
            throw new RuntimeException("Bạn không có quyền nạp (ingest) tài liệu này!");
        }

        if (doc.getFiles() == null || doc.getFiles().isEmpty()) {
            return new IngestResponseDTO(documentId, "NO_FILE", 0, "Tài liệu chưa có file đính kèm.");
        }
        DocFile docFile = doc.getFiles().getFirst();

        IngestFormat format = resolveIngestFormat(docFile);
        if (format == IngestFormat.UNSUPPORTED) {
            // Không phải lỗi — chỉ đơn giản là định dạng này vô nghĩa khi đọc như text (ảnh,
            // video, audio, file nén, thực thi...). Lưu lại trạng thái để FE biết mà không cần
            // gọi lại ingest.
            doc.setIngestStatus(IngestStatus.UNSUPPORTED_FORMAT);
            docDocumentRepository.save(doc);
            return new IngestResponseDTO(documentId, "UNSUPPORTED_FORMAT", 0,
                    "Định dạng không hỗ trợ đọc AI (nhận được: " + docFile.getFileType() + ").");
        }

        Resource resource;
        try {
            resource = resolveResource(docFile);
        }
        catch (MalformedURLException e) {
            return new IngestResponseDTO(documentId, "FILE_ERROR", 0, "Không đọc được file: " + e.getMessage());
        }

        // PDF giữ page-aware reader (có trang thật). Mọi định dạng khác (không nằm trong
        // blocklist) đọc qua Tika — không có khái niệm trang, chunk sẽ có page=null (citation
        // layer đã hỗ trợ nullable page).
        boolean hasRealPages = format == IngestFormat.PDF;
        List<Document> rawDocs = hasRealPages
                ? new PagePdfDocumentReader(resource).get()
                : new TikaDocumentReader(resource).get();

        // Content-quality gate: Tika "đọc được" một file không có nghĩa là nội dung hữu ích
        // (file rỗng, ảnh quét không OCR, định dạng lạ Tika chỉ trích ra vài ký tự rác...).
        // Chỉ áp dụng cho nhánh Tika — PDF page-aware reader không qua bước này.
        if (!hasRealPages && nonWhitespaceLength(rawDocs) < MIN_USEFUL_CONTENT_CHARS) {
            doc.setIngestStatus(IngestStatus.UNSUPPORTED_FORMAT);
            docDocumentRepository.save(doc);
            return new IngestResponseDTO(documentId, "UNSUPPORTED_FORMAT", 0,
                    "Không trích xuất được nội dung hữu ích từ file (rỗng hoặc không phải văn bản thật).");
        }

        List<Document> chunks = new TokenTextSplitter().apply(rawDocs);

        if (chunks.isEmpty()) {
            return new IngestResponseDTO(documentId, "EMPTY", 0, "Không trích xuất được nội dung nào từ tài liệu.");
        }

        // Idempotent re-ingest: xoá dữ liệu ingest cũ của tài liệu này trước khi ghi lại.
        docEmbeddingRepository.deleteByDocument_Id(documentId);
        vectorStore.delete(new FilterExpressionBuilder().eq("documentId", documentId.intValue()).build());

        List<DocEmbedding> rows = new ArrayList<>(chunks.size());
        List<SimpleVectorStoreContent> vectorContents = new ArrayList<>(chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            Document chunk = chunks.get(i);
            String text = chunk.getText();
            Integer page = hasRealPages
                    ? (Integer) chunk.getMetadata().get(PagePdfDocumentReader.METADATA_START_PAGE_NUMBER)
                    : null;

            float[] vector = embedWithRetry(text, documentId, i);

            rows.add(DocEmbedding.builder()
                    .document(doc)
                    .chunkIndex(i)
                    .chunkText(text)
                    .page(page)
                    .embedding(toJson(vector))
                    .build());

            vectorContents.add(new SimpleVectorStoreContent(
                    vectorId(documentId, i), text,
                    chunkMetadata(documentId, doc.getTitle(), authorOf(doc), page, i), vector));
        }

        docEmbeddingRepository.saveAll(rows);
        vectorStore.hydrate(vectorContents);

        doc.setIngestStatus(IngestStatus.INGESTED);
        docDocumentRepository.save(doc);

        String message = hasRealPages
                ? "Đã nạp " + chunks.size() + " đoạn từ " + rawDocs.size() + " trang."
                : "Đã nạp " + chunks.size() + " đoạn.";
        return new IngestResponseDTO(documentId, "INGESTED", chunks.size(), message);
    }

    private enum IngestFormat { PDF, TIKA, UNSUPPORTED }

    // Format policy (blocklist): PDF giữ page-aware reader riêng. Mọi định dạng KHÁC được thử
    // qua Tika, TRỪ những định dạng vô nghĩa khi đọc như text (ảnh/video/audio/nén/thực thi —
    // xem BLOCKED_EXTENSIONS). Mục tiêu là đọc được càng nhiều định dạng càng tốt, nên đây là
    // whitelist ngược (chặn cái biết chắc vô nghĩa) thay vì liệt kê từng định dạng được phép.
    private IngestFormat resolveIngestFormat(DocFile docFile) {
        String type = docFile.getFileType() != null ? docFile.getFileType().toLowerCase() : "";
        String name = docFile.getFileName() != null ? docFile.getFileName().toLowerCase() : "";

        if (type.contains("pdf") || name.endsWith(".pdf")) {
            return IngestFormat.PDF;
        }

        // MIME prefix bắt được phần lớn ảnh/video/audio kể cả khi phần mở rộng lạ/thiếu.
        boolean blockedByMime = type.startsWith("image/") || type.startsWith("video/") || type.startsWith("audio/");
        // Extension là lưới an toàn cho archive/executable và cho trường hợp MIME bị thiếu
        // hoặc chung chung (vd. "application/octet-stream").
        boolean blockedByExtension = BLOCKED_EXTENSIONS.stream().anyMatch(name::endsWith);
        if (blockedByMime || blockedByExtension) {
            return IngestFormat.UNSUPPORTED;
        }

        return IngestFormat.TIKA;
    }

    private static int nonWhitespaceLength(List<Document> docs) {
        int count = 0;
        for (Document d : docs) {
            String text = d.getText();
            if (text == null) continue;
            for (int i = 0; i < text.length(); i++) {
                if (!Character.isWhitespace(text.charAt(i))) count++;
            }
        }
        return count;
    }

    // Retry-with-backoff quanh 1 lệnh gọi Gemini embed. Chỉ retry lỗi tạm thời
    // (429, timeout/IO, 5xx); lỗi 4xx khác (input xấu, auth sai...) ném ngay, không retry.
    // Hết lượt retry -> ném RuntimeException rõ ràng nêu document/chunk nào fail; vì được
    // gọi TRƯỚC saveAll()/hydrate() trong ingest(), @Transactional sẽ rollback toàn bộ,
    // không để lại DocEmbedding rows dở dang cho tài liệu đó.
    // Package-private (không phải private) để unit test trong cùng package gọi trực tiếp.
    float[] embedWithRetry(String text, Long documentId, int chunkIndex) {
        long backoffMs = INITIAL_BACKOFF_MS;
        for (int attempt = 1; attempt <= MAX_EMBED_ATTEMPTS; attempt++) {
            try {
                return embeddingModel.embed(text);
            }
            catch (RuntimeException ex) {
                boolean lastAttempt = attempt == MAX_EMBED_ATTEMPTS;
                if (!isTransientEmbeddingFailure(ex) || lastAttempt) {
                    log.error("Embed thất bại vĩnh viễn cho document={} chunk={} (lần thử {}/{}): {}",
                            documentId, chunkIndex, attempt, MAX_EMBED_ATTEMPTS, ex.getMessage());
                    throw new RuntimeException(
                            "Không thể embed đoạn #" + chunkIndex + " của tài liệu " + documentId
                                    + " sau " + attempt + " lần thử: " + ex.getMessage(), ex);
                }
                log.warn("Embed tạm thời thất bại cho document={} chunk={} (lần thử {}/{}): {} — thử lại sau {}ms",
                        documentId, chunkIndex, attempt, MAX_EMBED_ATTEMPTS, ex.getMessage(), backoffMs);
                sleep(backoffMs);
                backoffMs *= 2;
            }
        }
        // Không bao giờ tới đây: vòng lặp luôn return hoặc throw ở lần thử cuối.
        throw new IllegalStateException("Unreachable");
    }

    // 429 (rate limit) và mọi 5xx là tạm thời -> retry. Các mã 4xx khác (400 bad request,
    // 401/403 auth, 404 model not found...) là lỗi cố định -> retry không giúp gì, ném ngay.
    private boolean isTransientEmbeddingFailure(RuntimeException ex) {
        if (ex instanceof GenAiIOException) {
            return true;
        }
        if (ex instanceof ApiException apiEx) {
            int code = apiEx.code();
            return code == 429 || code >= 500;
        }
        return false;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ingest bị gián đoạn trong lúc chờ retry embedding", ie);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void hydrateFromDatabase() {
        List<DocEmbedding> all = docEmbeddingRepository.findAllWithDocument();
        List<SimpleVectorStoreContent> contents = new ArrayList<>(all.size());

        for (DocEmbedding e : all) {
            try {
                float[] vector = fromJson(e.getEmbedding());
                Long documentId = e.getDocument().getId();
                contents.add(new SimpleVectorStoreContent(
                        vectorId(documentId, e.getChunkIndex()), e.getChunkText(),
                        chunkMetadata(documentId, e.getDocument().getTitle(), authorOf(e.getDocument()), e.getPage(), e.getChunkIndex()),
                        vector));
            }
            catch (Exception ex) {
                // Bỏ qua dòng lỗi (embedding JSON hỏng...) — không chặn khởi động app vì 1 dòng xấu.
            }
        }

        if (!contents.isEmpty()) {
            vectorStore.hydrate(contents);
        }
    }

    @Override
    public List<Document> retrieveChunks(String query, List<Long> documentIds, int topK, double similarityThreshold) {
        if (documentIds == null || documentIds.isEmpty()) {
            return List.of();
        }
        // SimpleVectorStore's IN filter compiles to SpEL "{...}.contains(#metadata['k'])",
        // which uses strict List.contains()/equals() — a bare SpEL numeric literal parses
        // as Integer, so a stored Long documentId would never match (Integer.equals(Long)
        // is always false). Metadata is stored as Integer (see chunkMetadata) to match.
        List<Object> ids = documentIds.stream().map(id -> (Object) id.intValue()).collect(Collectors.toList());
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .filterExpression(new FilterExpressionBuilder().in("documentId", ids).build())
                .build();
        return vectorStore.similaritySearch(request);
    }

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authAccountRepository.findByIdentifier(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user đang đăng nhập"))
                .getUser().getId();
    }

    private boolean isAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    // Cùng logic resolve local-disk-vs-Cloudinary với DocumentController.resolveResource
    // (giữ nguyên tách biệt để không đụng vào controller CRUD tài liệu đang chạy tốt).
    private Resource resolveResource(DocFile docFile) throws MalformedURLException {
        String fileUrl = docFile.getFileUrl();
        if (fileUrl != null && fileUrl.startsWith("http")) {
            return new UrlResource(new URL(fileUrl));
        }
        Path filePath = Paths.get(uploadDir).resolve(fileUrl).normalize();
        return new UrlResource(filePath.toUri());
    }

    private static String vectorId(Long documentId, int chunkIndex) {
        return "doc-" + documentId + "-chunk-" + chunkIndex;
    }

    private static Map<String, Object> chunkMetadata(Long documentId, String documentTitle, String author, Integer page, int chunkIndex) {
        Map<String, Object> metadata = new HashMap<>();
        // Integer, not Long — see the comment in retrieveChunks() for why.
        metadata.put("documentId", documentId.intValue());
        metadata.put("documentTitle", documentTitle);
        metadata.put("author", author);
        // Chỉ set nếu có trang thật (PDF/PPTX...) — định dạng không trang (TXT/DOCX...) để trống,
        // AiChatService.pageOf() đọc absent-or-null đều ra null -> CitationDTO.page = null.
        if (page != null) {
            metadata.put("page", page);
        }
        metadata.put("chunkIndex", chunkIndex);
        return metadata;
    }

    // Author hiện lấy từ chủ sở hữu tài liệu (uploader) — schema chưa có field "author" tách
    // biệt (vd. tác giả học thuật khác người upload). Null-safe vì fullName có thể chưa set.
    private static String authorOf(DocDocument doc) {
        return doc.getUser() != null ? doc.getUser().getFullName() : null;
    }

    private String toJson(float[] vector) {
        try {
            return objectMapper.writeValueAsString(vector);
        }
        catch (Exception e) {
            throw new RuntimeException("Không serialize được embedding vector", e);
        }
    }

    private float[] fromJson(String json) {
        try {
            return objectMapper.readValue(json, float[].class);
        }
        catch (Exception e) {
            throw new RuntimeException("Không parse được embedding JSON", e);
        }
    }
}
