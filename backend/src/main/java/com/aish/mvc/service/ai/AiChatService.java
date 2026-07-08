package com.aish.mvc.service.ai;

import com.aish.mvc.dto.ai.AiChatRequest;
import com.aish.mvc.dto.ai.AiChatResponse;
import com.aish.mvc.dto.ai.CitationDTO;
import com.aish.mvc.dto.ai.RecommendedDocumentDTO;
import com.aish.mvc.dto.ai.RelatedDocDTO;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.service.ai.AiRecommendationService;
import com.aish.mvc.service.doc.DocEmbeddingService;
import com.aish.mvc.service.doc.DocumentAccessPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    private static final int TOP_K = 4;
    // Đo thực tế với gemini-embedding-001 (768d): câu hỏi liên quan trực tiếp tới 1 trang
    // ra ~0.69 cosine similarity; câu hỏi hoàn toàn không liên quan vẫn ra ~0.41-0.45 (không
    // phải 0 — Gemini's embedding space có "sàn" khá cao). 0.55 nằm giữa 2 vùng này. Đây là
    // hiệu chỉnh trên 1 tài liệu demo nhỏ — cần tinh chỉnh lại khi có dữ liệu thật đa dạng hơn.
    private static final double SIMILARITY_THRESHOLD = 0.55;
    private static final int SNIPPET_LENGTH = 240;
    // relatedDocs là side-channel gợi ý (DEC-027) — cố tình nhỏ, không phải kết quả chính.
    private static final int RELATED_LIMIT = 3;

    private final ChatClient chatClient;
    private final DocEmbeddingService docEmbeddingService;
    private final DocumentAccessPort documentAccessPort;
    private final DocDocumentRepository docDocumentRepository;
    private final AuthAccountRepository authAccountRepository;
    private final AiRecommendationService aiRecommendationService;

    // Thông tin hệ thống — dùng cho GENERAL mode (không tìm thấy đoạn tài liệu liên quan)
    private static final String SYSTEM_PROMPT = """
            Bạn là AI HiveMind — trợ lý AI của nền tảng HiveMind dành cho sinh viên.

            Thông tin hệ thống HiveMind:
            - HiveMind là nền tảng hỗ trợ học tập bằng AI
            - Người dùng có thể upload tài liệu PDF và đặt câu hỏi về nội dung tài liệu
            - Hỗ trợ chat AI thông minh, tìm kiếm tài liệu, và quản lý tài liệu cá nhân
            - Tài liệu có thể để PUBLIC (mọi người xem) hoặc PRIVATE (chỉ mình xem)
            - Được xây dựng bởi nhóm 6 SWP391 SE1901 SU26

            Nguyên tắc trả lời:
            - Nếu câu hỏi liên quan đến HiveMind → trả lời dựa trên thông tin hệ thống trên
            - Nếu không liên quan → trả lời như AI thông thường
            - Luôn trả lời thân thiện, ngắn gọn, bằng tiếng Việt
            """;

    private static final String RAG_PROMPT_TEMPLATE = """
            Bạn là AI HiveMind — trợ lý AI của nền tảng HiveMind. Dưới đây là các đoạn trích từ (các) tài liệu người dùng đang hỏi.
            Chỉ trả lời dựa trên nội dung trích dẫn bên dưới. Nếu trích dẫn không đủ để trả lời,
            hãy nói rõ là tài liệu không có thông tin đó, đừng bịa thêm.
            Luôn trả lời ngắn gọn, chính xác, bằng tiếng Việt.

            Trích dẫn tài liệu:
            %s
            """;

    /**
     * Early-stop hybrid (DEC-027): thử từng tier theo thứ tự ưu tiên, dừng ngay ở tier
     * đầu tiên có kết quả đủ tốt (non-empty sau ngưỡng similarity).
     * Tier 1: tài liệu đang mở (documentId trong request), nếu có — DEC-011 kiểm tra quyền xem trước.
     * Tier 2: các tài liệu khác của chính user đang đăng nhập (không đụng private của người khác).
     * Tier 3: GENERAL — không tìm thấy đoạn liên quan, trả lời bằng kiến thức chung (DEC-028).
     */
    public AiChatResponse chat(AiChatRequest request) {
        String message = request.getMessage();
        Long documentId = request.getDocumentId();
        Long currentUserId = currentUserIdOrNull();

        if (documentId != null) {
            if (!documentAccessPort.isAvailableTo(documentId, currentUserId)) {
                throw new RuntimeException("Bạn không có quyền hỏi AI về tài liệu này!");
            }
            List<Document> hits = docEmbeddingService.retrieveChunks(message, List.of(documentId), TOP_K, SIMILARITY_THRESHOLD);
            if (!hits.isEmpty()) {
                return buildRagResponse(message, hits, currentUserId);
            }
        }

        if (currentUserId != null) {
            List<Long> ownDocIds = docDocumentRepository.findByDeletedAtIsNullAndUser_Id(currentUserId).stream()
                    .map(DocDocument::getId)
                    .filter(id -> !id.equals(documentId))
                    .collect(Collectors.toList());
            if (!ownDocIds.isEmpty()) {
                List<Document> hits = docEmbeddingService.retrieveChunks(message, ownDocIds, TOP_K, SIMILARITY_THRESHOLD);
                if (!hits.isEmpty()) {
                    return buildRagResponse(message, hits, currentUserId);
                }
            }
        }

        return buildGeneralResponse(message, currentUserId);
    }

    private AiChatResponse buildRagResponse(String userMessage, List<Document> hits, Long currentUserId) {
        String context = hits.stream()
                .map(d -> "[Trang " + pageOf(d) + "] " + d.getText())
                .collect(Collectors.joining("\n\n---\n\n"));

        Prompt prompt = new Prompt(List.of(
                new SystemMessage(String.format(RAG_PROMPT_TEMPLATE, context)),
                new UserMessage(userMessage)
        ));

        String answer = chatClient.prompt(prompt).call().content();

        List<CitationDTO> citations = hits.stream()
                .map(d -> new CitationDTO(
                        documentIdOf(d),
                        titleOf(d),
                        authorOf(d),
                        pageOf(d),
                        snippet(d.getText())))
                .collect(Collectors.toList());

        // Side-channel gợi ý (DEC-027), KHÔNG ảnh hưởng câu trả lời chính — dựa trên tài liệu
        // đang được hỏi (chunk có điểm cao nhất) làm seed cho AiRecommendationService.
        List<RelatedDocDTO> relatedDocs = relatedToTopHit(hits, currentUserId);

        return new AiChatResponse(answer, "RAG", citations, relatedDocs);
    }

    private AiChatResponse buildGeneralResponse(String userMessage, Long currentUserId) {
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(SYSTEM_PROMPT),
                new UserMessage(userMessage)
        ));
        String answer = chatClient.prompt(prompt).call().content();

        // DEC-041: user hỏi về chủ đề mà họ KHÔNG có tài liệu nào (mới rơi vào GENERAL) ->
        // thử gợi ý tài liệu PUBLIC liên quan tới CHÍNH câu hỏi bằng embedding similarity.
        // Không tốn thêm lượt gọi LLM (chỉ 1 vector search), giữ đúng tinh thần "lightweight".
        List<RelatedDocDTO> relatedDocs = suggestPublicDocsForTopic(userMessage, currentUserId);

        return new AiChatResponse(answer, "GENERAL", List.of(), relatedDocs);
    }

    private List<RelatedDocDTO> relatedToTopHit(List<Document> hits, Long currentUserId) {
        Long seedDocId = hits.isEmpty() ? null : documentIdOf(hits.get(0));
        if (seedDocId == null) return List.of();
        try {
            return aiRecommendationService.recommendRelatedToDocument(seedDocId, currentUserId, RELATED_LIMIT).stream()
                    .map(r -> new RelatedDocDTO(r.getDocumentId(), r.getTitle(), r.getOwnerName()))
                    .collect(Collectors.toList());
        }
        catch (Exception e) {
            // relatedDocs là gợi ý phụ — lỗi ở đây không được làm hỏng câu trả lời chính.
            log.warn("Không lấy được relatedDocs cho seedDoc={}: {}", seedDocId, e.getMessage());
            return List.of();
        }
    }

    private List<RelatedDocDTO> suggestPublicDocsForTopic(String message, Long currentUserId) {
        try {
            List<Long> publicDocIds = docDocumentRepository
                    .findPublicApprovedDocuments(DocumentVisibility.PUBLIC, ModerationStatus.APPROVED)
                    .stream()
                    .map(DocDocument::getId)
                    .filter(id -> documentAccessPort.isAvailableTo(id, currentUserId))
                    .collect(Collectors.toList());
            if (publicDocIds.isEmpty()) return List.of();

            List<Document> hits = docEmbeddingService.retrieveChunks(message, publicDocIds, RELATED_LIMIT, SIMILARITY_THRESHOLD);
            if (hits.isEmpty()) return List.of();

            Set<Long> uniqueIds = new LinkedHashSet<>();
            for (Document hit : hits) {
                Long docId = documentIdOf(hit);
                if (docId != null) uniqueIds.add(docId);
            }
            if (uniqueIds.isEmpty()) return List.of();

            Map<Long, DocDocument> byId = docDocumentRepository.findAllById(uniqueIds).stream()
                    .collect(Collectors.toMap(DocDocument::getId, d -> d));

            return uniqueIds.stream()
                    .map(byId::get)
                    .filter(java.util.Objects::nonNull)
                    .map(d -> new RelatedDocDTO(d.getId(), d.getTitle(), d.getUser() != null ? d.getUser().getFullName() : null))
                    .collect(Collectors.toList());
        }
        catch (Exception e) {
            log.warn("Không gợi ý được tài liệu PUBLIC cho GENERAL mode: {}", e.getMessage());
            return List.of();
        }
    }

    // null = guest hoặc chưa đăng nhập — /api/ai/chat vẫn public, chỉ giới hạn tier 2 khi có user.
    private Long currentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return authAccountRepository.findByIdentifier(auth.getName())
                .map(a -> a.getUser().getId())
                .orElse(null);
    }

    private static Integer pageOf(Document d) {
        Object page = d.getMetadata().get("page");
        return page instanceof Integer ? (Integer) page : null;
    }

    private static String titleOf(Document d) {
        Object title = d.getMetadata().get("documentTitle");
        return title instanceof String ? (String) title : null;
    }

    private static String authorOf(Document d) {
        Object author = d.getMetadata().get("author");
        return author instanceof String ? (String) author : null;
    }

    private static Long documentIdOf(Document d) {
        Object id = d.getMetadata().get("documentId");
        if (id instanceof Long l) return l;
        if (id instanceof Number n) return n.longValue();
        return null;
    }

    private static String snippet(String text) {
        if (text == null) return "";
        String trimmed = text.trim();
        return trimmed.length() <= SNIPPET_LENGTH ? trimmed : trimmed.substring(0, SNIPPET_LENGTH) + "...";
    }
}
