package com.aish.mvc.service.ai;

import com.aish.mvc.dto.ai.AiChatRequest;
import com.aish.mvc.dto.ai.AiChatResponse;
import com.aish.mvc.dto.ai.CitationDTO;
import com.aish.mvc.dto.ai.RelatedDocDTO;
import com.aish.mvc.entity.ai.AiConversation;
import com.aish.mvc.entity.ai.AiMessage;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.exception.QuotaExceededException;
import com.aish.mvc.repository.ai.AiUsageLogRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.service.config.SystemSettingService;
import com.aish.mvc.service.doc.DocEmbeddingService;
import com.aish.mvc.service.doc.DocumentAccessPort;
import com.aish.mvc.service.doc.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TRÁI TIM của tính năng CHAT VỚI AI. Có 2 chế độ trả lời:
 * <p>1) RAG (Retrieval-Augmented Generation) — khi câu hỏi gắn với một tài liệu cụ thể mà người
 * dùng có quyền xem: tìm các đoạn nội dung liên quan nhất trong tài liệu (qua
 * {@link com.aish.mvc.service.doc.DocEmbeddingService}) rồi bắt AI CHỈ trả lời dựa trên các đoạn
 * đó (kèm trích dẫn trang).
 * <p>2) GENERAL — không gắn tài liệu, hoặc tài liệu không tìm được đoạn nào liên quan: AI trả lời
 * như trợ lý thông thường, có thể tự gọi thêm "công cụ" ({@link AdminAiTools}, {@link UserAiTools})
 * để tra dữ liệu thật của hệ thống thay vì bịa.
 * <p>Người dùng đã đăng nhập bị giới hạn số lượt chat/số token mỗi ngày (quota); khách (chưa đăng
 * nhập) được chat nhưng không lưu lịch sử và không dùng được các "công cụ" cá nhân.
 */
@Service
@RequiredArgsConstructor
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    // Giá trị fallback nếu SystemSetting chưa có key/lỗi đọc — giữ nguyên giá trị hard-code cũ.
    private static final int TOP_K = 4;
    private static final int RECENT_MESSAGE_LIMIT = 10;
    private static final double SIMILARITY_THRESHOLD = 0.55;
    private static final int SNIPPET_LENGTH = 240;
    private static final int RELATED_LIMIT = 3;

    // callType dùng khi log usage cho các lượt chat (RAG + GENERAL) - dùng để enforce quota/ngày (AIU-4).
    private static final Set<String> CHAT_CALL_TYPES = Set.of("CHAT_RAG", "CHAT_GENERAL");

    private final ChatClient chatClient;
    private final DocEmbeddingService docEmbeddingService;
    private final DocumentAccessPort documentAccessPort;
    private final DocDocumentRepository docDocumentRepository;
    private final RecommendationService recommendationService;
    private final AiConversationService aiConversationService;
    private final AdminAiTools adminAiTools;
    private final UserAiTools userAiTools;
    private final AiUsageTracker aiUsageTracker;
    private final SystemSettingService systemSettingService;
    private final AiUsageLogRepository aiUsageLogRepository;

    private static final String SYSTEM_PROMPT = """
            Bạn là AI HiveMind - trợ lý AI của nền tảng HiveMind dành cho sinh viên.

            Thông tin hệ thống HiveMind:
            - HiveMind là nền tảng hỗ trợ học tập bằng AI
            - Người dùng có thể upload tài liệu PDF và đặt câu hỏi về nội dung tài liệu
            - Hỗ trợ chat AI thông minh, tìm kiếm tài liệu, và quản lý tài liệu cá nhân
            - Tài liệu có thể để PUBLIC (mọi người xem) hoặc PRIVATE (chỉ mình xem)
            - Được xây dựng bởi nhóm 6 SWP391 SE1901 SU26

            Nguyên tắc trả lời:
            - Nếu câu hỏi liên quan đến HiveMind, trả lời dựa trên thông tin hệ thống trên
            - Nếu không liên quan, trả lời như AI thông thường
            - Luôn trả lời thân thiện, ngắn gọn, bằng tiếng Việt
            - Tuyệt đối không tiết lộ ID nội bộ, khóa chính, hay tên cột/bảng cơ sở dữ liệu trong câu trả lời.
            """;

    private static final String ADMIN_TOOLS_PROMPT = """

            Công cụ dành cho quản trị viên:
            - getSystemStats: dùng khi cần số liệu tổng quan hiện tại của toàn hệ thống.
            - searchDocuments: dùng khi cần tìm tài liệu theo tiêu đề hoặc lọc theo visibility, kiểm duyệt, môn học.
            - searchUsers: dùng khi cần tìm người dùng theo tên/email hoặc lọc theo vai trò, trạng thái.
            - getDocumentStatus: dùng khi cần trạng thái đầy đủ của một tài liệu có ID cụ thể.
            - getUserStatus: dùng khi cần trạng thái của một người dùng có ID hoặc email cụ thể.
            - getAiUsageStats: dùng khi cần số lượt gọi AI, token, chi phí hoặc phân loại usage hôm nay/7 ngày.
            - Luôn trả lời bằng tiếng Việt và chép nguyên văn toàn bộ dòng/trường dữ liệu công cụ trả về, không diễn giải lại hoặc bỏ sót; tuyệt đối không tự đoán số, tên hay trạng thái.
            - Luôn nhắc ID của tài liệu/người dùng trong câu trả lời để quản trị viên tìm được trên trang quản trị.
            - Tuyệt đối không nhắc tên công cụ (vd. "getSystemStats", "searchDocuments") hay nói rằng bạn vừa gọi một công cụ/tool/hàm nào đó; trình bày dữ liệu một cách tự nhiên như thể bạn tự biết thông tin đó.
            """;

    private static final String USER_TOOLS_PROMPT = """

            Công cụ dữ liệu cá nhân dành cho người đang trò chuyện:
            - getMyStats: dùng khi người dùng hỏi thống kê tài liệu, lượt yêu thích hoặc report của chính họ.
            - searchMyDocuments: dùng khi người dùng muốn tìm tài liệu họ có quyền truy cập.
            - getMyDocumentStatus: dùng khi người dùng hỏi trạng thái một tài liệu cụ thể của chính họ theo tiêu đề/từ khóa.
            - getMyReportStatus: dùng khi người dùng hỏi trạng thái các report chính họ đã gửi.
            - Tất cả công cụ có chữ "My" tự động dùng danh tính của người đang trò chuyện; không yêu cầu và không tự chọn userId/email chủ dữ liệu.
            - Trả lời bằng tiếng Việt, chép nguyên văn dữ liệu công cụ trả về và tuyệt đối không bịa thêm bản ghi.
            - Không tiết lộ ID nội bộ hay tên trường DB; nhắc tài liệu bằng tiêu đề.
            - Tuyệt đối không nhắc tên công cụ (vd. "getMyDocumentStatus", "searchMyDocuments") hay nói rằng bạn vừa gọi một công cụ/tool/hàm nào đó; trình bày dữ liệu một cách tự nhiên như thể bạn tự biết thông tin đó.
            """;

    private static final String RAG_PROMPT_TEMPLATE = """
            Bạn là AI HiveMind - trợ lý AI của nền tảng HiveMind. Dưới đây là các đoạn trích từ tài liệu người dùng đang hỏi.
            Chỉ trả lời dựa trên nội dung trích dẫn bên dưới. Nếu trích dẫn không đủ để trả lời,
            hãy nói rõ là tài liệu không có thông tin đó, đừng bịa thêm.
            Luôn trả lời ngắn gọn, chính xác, bằng tiếng Việt.

            Trích dẫn tài liệu:
            %s
            """;

    // ĐIỂM VÀO CHÍNH của tính năng chat AI. Đầu vào: câu hỏi + (tuỳ chọn) id cuộc trò chuyện/id
    // tài liệu đang hỏi. Trả về: câu trả lời kèm trích dẫn (nếu RAG) và tài liệu liên quan gợi ý.
    // Các bước: (1) xác định user hiện tại, chưa đăng nhập thì bỏ qua kiểm tra quota;
    // (2) đọc cấu hình hệ thống (topK, ngưỡng tương đồng, số tin nhắn nhớ gần nhất) một lần duy
    // nhất; (3) xác định tài liệu đang hỏi (từ request hoặc từ cuộc trò chuyện cũ);
    // (4) có tài liệu và tìm được đoạn nội dung liên quan -> trả lời theo chế độ RAG;
    // (5) không thì trả lời theo chế độ GENERAL (có thể AI tự gọi thêm công cụ tra dữ liệu);
    // (6) lưu lại lượt hỏi-đáp nếu đã đăng nhập.
    public AiChatResponse chat(AiChatRequest request) {
        String message = request.getMessage();
        AuthUser currentUser = aiConversationService.currentUserOrNull();
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        if (currentUser != null) {
            enforceDailyQuota(currentUserId);
        }
        AiConversation conversation = null;

        // Đọc 1 lần đầu mỗi request — KHÔNG đọc lặp lại trong các nhánh/vòng lặp bên dưới.
        int topK = systemSettingService.getInt(SystemSettingService.AI_TOP_K_KEY, TOP_K);
        double similarityThreshold = systemSettingService.getDouble(
                SystemSettingService.AI_SIMILARITY_THRESHOLD_KEY, SIMILARITY_THRESHOLD);
        int recentMessageLimit = systemSettingService.getInt(
                SystemSettingService.AI_RECENT_MESSAGE_LIMIT_KEY, RECENT_MESSAGE_LIMIT);

        if (currentUser != null && request.getConversationId() != null) {
            conversation = aiConversationService.requireOwnedConversation(request.getConversationId(), currentUser);
        }

        DocumentResolution resolution = resolveDocument(request, conversation, currentUserId);
        List<AiMessage> recentMessages = conversation == null
                ? List.of()
                : aiConversationService.getRecentMessages(conversation.getId(), recentMessageLimit);

        AiChatResponse response;
        if (resolution.documentId() != null) {
            // Tìm các đoạn nội dung trong tài liệu có ý nghĩa gần với câu hỏi nhất (similarity search).
            List<Document> hits = docEmbeddingService.retrieveChunks(
                    message,
                    List.of(resolution.documentId()),
                    topK,
                    similarityThreshold);
            if (!hits.isEmpty()) {
                response = buildRagResponse(message, hits, currentUserId, recentMessages);
                persistIfAuthenticated(currentUser, request, response);
                return response;
            }
        }

        response = resolution.documentUnavailable()
                ? buildUnavailableDocumentGeneralResponse(
                        message, currentUser, currentUserId, recentMessages, similarityThreshold)
                : buildGeneralResponse(message, currentUser, currentUserId, recentMessages, similarityThreshold);
        persistIfAuthenticated(currentUser, request, response);
        return response;
    }

    // Chỉ áp cho user đã đăng nhập (ẩn danh bỏ qua - khe hở đã chấp nhận). Cap <= 0 = tắt giới hạn.
    // Kiểm TRƯỚC khi retrieve/gọi LLM để không tốn chi phí cho lượt bị chặn.
    private void enforceDailyQuota(Long currentUserId) {
        int callCap = systemSettingService.getInt(
                SystemSettingService.AI_CHAT_DAILY_CALL_CAP_KEY, SystemSettingService.AI_CHAT_DAILY_CALL_CAP_DEFAULT);
        long tokenCap = systemSettingService.getLong(
                SystemSettingService.AI_CHAT_DAILY_TOKEN_CAP_KEY, SystemSettingService.AI_CHAT_DAILY_TOKEN_CAP_DEFAULT);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        if (callCap > 0 && aiUsageLogRepository.countChatCallsForUserSince(currentUserId, CHAT_CALL_TYPES, startOfDay) >= callCap) {
            throw new QuotaExceededException("error.ai.quotaCallsExceeded"); // vượt số lượt chat/ngày
        }
        if (tokenCap > 0 && aiUsageLogRepository.sumChatTokensForUserSince(currentUserId, CHAT_CALL_TYPES, startOfDay) >= tokenCap) {
            throw new QuotaExceededException("error.ai.quotaTokensExceeded"); // vượt số token/ngày
        }
    }

    // Xác định tài liệu nào đang được hỏi tới, và có còn xem được không.
    // Ưu tiên: id tài liệu gửi kèm trong request (phải còn quyền xem, không thì chặn hẳn) ->
    // tài liệu gắn với cuộc trò chuyện cũ (còn quyền xem thì dùng tiếp, hết quyền thì đánh dấu
    // "documentUnavailable" để chuyển sang trả lời GENERAL kèm lời giải thích) -> không có tài
    // liệu nào thì chat kiểu chung.
    private DocumentResolution resolveDocument(
            AiChatRequest request,
            AiConversation conversation,
            Long currentUserId) {
        if (request.getDocumentId() != null) {
            if (!documentAccessPort.isAvailableTo(request.getDocumentId(), currentUserId)) {
                throw new RuntimeException("error.ai.askForbidden");
            }
            return new DocumentResolution(request.getDocumentId(), false);
        }

        if (conversation != null && conversation.getDocument() != null) {
            Long conversationDocumentId = conversation.getDocument().getId();
            if (documentAccessPort.isAvailableTo(conversationDocumentId, currentUserId)) {
                return new DocumentResolution(conversationDocumentId, false);
            }
            return new DocumentResolution(null, true);
        }

        return new DocumentResolution(null, false);
    }

    private record DocumentResolution(Long documentId, boolean documentUnavailable) {}

    // Guest (chưa đăng nhập) không lưu lịch sử chat — chỉ lưu khi đã xác định được user.
    private void persistIfAuthenticated(AuthUser currentUser, AiChatRequest request, AiChatResponse response) {
        if (currentUser == null) return;

        AiConversation conversation = aiConversationService.persistExchange(
                currentUser,
                request.getConversationId(),
                request.getDocumentId(),
                request.getMessage(),
                response.getAnswer());
        response.setConversationId(conversation.getId());
    }

    // Trả lời theo chế độ RAG: chỉ dựa trên các đoạn trích (hits) tìm được trong tài liệu.
    // Các bước: (1) ghép các đoạn trích thành 1 khối "ngữ cảnh" kèm số trang; (2) dựng prompt
    // gồm system prompt (chứa ngữ cảnh) + vài tin nhắn gần đây (giữ mạch hội thoại) + câu hỏi mới;
    // (3) gọi AI; (4) ghi lại lượt dùng AI; (5) dựng danh sách trích dẫn (trang + đoạn trích) và
    // gợi ý thêm vài tài liệu liên quan tới tài liệu đang hỏi.
    private AiChatResponse buildRagResponse(
            String userMessage,
            List<Document> hits,
            Long currentUserId,
            List<AiMessage> recentMessages) {
        String context = hits.stream()
                .map(d -> "[Trang " + pageOf(d) + "] " + d.getText())
                .collect(Collectors.joining("\n\n---\n\n"));

        List<Message> promptMessages = new ArrayList<>();
        promptMessages.add(new SystemMessage(String.format(RAG_PROMPT_TEMPLATE, context)));
        promptMessages.addAll(toChatMessages(recentMessages));
        promptMessages.add(new UserMessage(userMessage));
        Prompt prompt = new Prompt(promptMessages);

        ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse(); // gọi AI
        String answer = chatResponse.getResult().getOutput().getText();
        aiUsageTracker.log("CHAT_RAG", chatResponse, null); // ghi lại số token đã dùng

        List<CitationDTO> citations = hits.stream()
                .map(d -> new CitationDTO(
                        documentIdOf(d),
                        titleOf(d),
                        authorOf(d),
                        pageOf(d),
                        snippet(d.getText())))
                .collect(Collectors.toList());

        List<RelatedDocDTO> relatedDocs = relatedToTopHit(hits, currentUserId);

        return new AiChatResponse(answer, "RAG", citations, relatedDocs);
    }

    // Chuyển lịch sử tin nhắn đã lưu (AiMessage, entity của DB) sang định dạng Message của Spring
    // AI (UserMessage/AssistantMessage) để đưa vào prompt gọi AI tiếp theo.
    private List<Message> toChatMessages(List<AiMessage> messages) {
        return messages.stream()
                .map(message -> switch (message.getRole()) {
                    case USER -> new UserMessage(message.getContent());
                    case ASSISTANT -> new AssistantMessage(message.getContent());
                })
                .map(Message.class::cast)
                .toList();
    }

    // Trả lời theo chế độ GENERAL (không dựa trên tài liệu cụ thể).
    // Các bước: (1) chọn system prompt theo vai trò — Admin được thêm mô tả công cụ quản trị +
    // công cụ cá nhân, user thường chỉ có công cụ cá nhân, guest không có công cụ nào;
    // (2) gắn danh sách "công cụ" tương ứng để AI có thể tự gọi khi cần tra dữ liệu thật;
    // (3) gọi AI; (4) không phải Admin thì lọc bỏ mọi ID nội bộ lỡ lọt vào câu trả lời (lưới an
    // toàn cuối, phòng khi AI phá rào dặn dò trong prompt); (5) ghi lại lượt dùng AI; (6) gợi ý
    // thêm vài tài liệu PUBLIC liên quan tới chủ đề đang hỏi.
    private AiChatResponse buildGeneralResponse(
            String userMessage,
            AuthUser user,
            Long currentUserId,
            List<AiMessage> recentMessages,
            double similarityThreshold) {
        boolean isAdmin = user != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());
        boolean isAuthenticated = user != null;
        String systemPrompt = isAdmin
                ? SYSTEM_PROMPT + ADMIN_TOOLS_PROMPT + USER_TOOLS_PROMPT
                : isAuthenticated ? SYSTEM_PROMPT + USER_TOOLS_PROMPT : SYSTEM_PROMPT;

        ChatClient.ChatClientRequestSpec promptSpec = chatClient.prompt()
                .system(systemPrompt)
                .messages(toChatMessages(recentMessages))
                .user(userMessage);
        if (isAdmin) {
            promptSpec = promptSpec.tools(adminAiTools, userAiTools); // Admin dùng được cả 2 bộ công cụ
        } else if (isAuthenticated) {
            promptSpec = promptSpec.tools(userAiTools); // user thường chỉ dùng công cụ dữ liệu cá nhân
        }
        ChatResponse chatResponse = promptSpec.call().chatResponse(); // gọi AI
        String answer = chatResponse.getResult().getOutput().getText();
        if (!isAdmin) {
            answer = stripInternalIds(answer); // lọc bỏ ID nội bộ lỡ lọt ra (không áp cho Admin)
        }
        aiUsageTracker.log("CHAT_GENERAL", chatResponse, null); // ghi lại số token đã dùng

        List<RelatedDocDTO> relatedDocs = suggestPublicDocsForTopic(userMessage, currentUserId, similarityThreshold);

        return new AiChatResponse(answer, "GENERAL", List.of(), relatedDocs);
    }

    // Lưới an toàn cuối cùng cho luồng non-admin: dọn các pattern ID nội bộ rõ ràng
    // (nhãn ID + số, hoặc #số) phòng khi model vẫn lộ ID dù đã có rào ở system prompt.
    // KHÔNG áp cho admin (admin được nhắc ID để tra trên trang quản trị).
    private static final java.util.regex.Pattern INTERNAL_ID_LABELED_PATTERN = java.util.regex.Pattern.compile(
            "(?i)\\bID(\\s*người\\s*dùng|\\s*tài\\s*liệu|\\s*report)?\\s*[:#]?\\s*\\d+");
    private static final java.util.regex.Pattern INTERNAL_ID_HASH_PATTERN = java.util.regex.Pattern.compile("#\\d+");

    private static String stripInternalIds(String text) {
        if (text == null || text.isBlank()) return text;
        String result = INTERNAL_ID_LABELED_PATTERN.matcher(text).replaceAll("");
        result = INTERNAL_ID_HASH_PATTERN.matcher(result).replaceAll("");
        return result.replaceAll("[ \\t]{2,}", " ").strip();
    }

    // Trường hợp tài liệu gắn với cuộc trò chuyện cũ không còn xem được nữa (bị xoá/chuyển riêng
    // tư) -> vẫn trả lời GENERAL bình thường nhưng thêm câu giải thích ở đầu để người dùng hiểu vì sao.
    private AiChatResponse buildUnavailableDocumentGeneralResponse(
            String userMessage,
            AuthUser user,
            Long currentUserId,
            List<AiMessage> recentMessages,
            double similarityThreshold) {
        AiChatResponse response = buildGeneralResponse(userMessage, user, currentUserId, recentMessages, similarityThreshold);
        String answer = "Tài liệu gắn với cuộc trò chuyện này không còn khả dụng, nên mình sẽ trả lời ở chế độ GENERAL.\n\n"
                + response.getAnswer();
        return new AiChatResponse(answer, "GENERAL", response.getCitations(), response.getRelatedDocs());
    }

    // Gợi ý tài liệu liên quan tới tài liệu có đoạn trích khớp cao nhất (chế độ RAG). Lỗi thì bỏ
    // qua gợi ý (không làm hỏng câu trả lời chính).
    private List<RelatedDocDTO> relatedToTopHit(List<Document> hits, Long currentUserId) {
        Long seedDocId = hits.isEmpty() ? null : documentIdOf(hits.get(0));
        if (seedDocId == null) return List.of();
        try {
            return recommendationService.recommendRelatedToDocument(seedDocId, currentUserId, RELATED_LIMIT).stream()
                    .map(r -> new RelatedDocDTO(r.getDocumentId(), r.getTitle(), r.getOwnerName()))
                    .collect(Collectors.toList());
        }
        catch (Exception e) {
            log.warn("Không lấy được relatedDocs cho seedDoc={}: {}", seedDocId, e.getMessage());
            return List.of();
        }
    }

    // Chế độ GENERAL: tìm vài tài liệu PUBLIC đã duyệt có nội dung gần với câu hỏi (dùng chung cơ
    // chế similarity search với RAG, nhưng tìm trên TOÀN BỘ tài liệu công khai thay vì 1 tài liệu).
    // Lỗi thì bỏ qua gợi ý, không ảnh hưởng câu trả lời chính.
    private List<RelatedDocDTO> suggestPublicDocsForTopic(String message, Long currentUserId, double similarityThreshold) {
        try {
            List<Long> publicDocIds = docDocumentRepository
                    .findPublicApprovedDocuments(DocumentVisibility.PUBLIC, ModerationStatus.APPROVED)
                    .stream()
                    .map(DocDocument::getId)
                    .filter(id -> documentAccessPort.isAvailableTo(id, currentUserId))
                    .collect(Collectors.toList());
            if (publicDocIds.isEmpty()) return List.of();

            List<Document> hits = docEmbeddingService.retrieveChunks(message, publicDocIds, RELATED_LIMIT, similarityThreshold);
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

    // Các hàm dưới đây đọc metadata đính kèm mỗi đoạn trích (được gắn lúc ingest tài liệu, xem
    // DocEmbeddingServiceImpl) để dựng trích dẫn: số trang, tiêu đề tài liệu, tác giả, id tài liệu.
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

    // Cắt đoạn trích dài thành đoạn ngắn (kèm "...") để hiển thị gọn trong phần trích dẫn.
    private static String snippet(String text) {
        if (text == null) return "";
        String trimmed = text.trim();
        return trimmed.length() <= SNIPPET_LENGTH ? trimmed : trimmed.substring(0, SNIPPET_LENGTH) + "...";
    }
}
