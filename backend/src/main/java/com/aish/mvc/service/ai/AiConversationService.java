package com.aish.mvc.service.ai;

import com.aish.mvc.dto.ai.AiConversationSummaryDTO;
import com.aish.mvc.dto.ai.AiMessageDTO;
import com.aish.mvc.dto.ai.ModerationDecision;
import com.aish.mvc.dto.ai.ModerationResultDTO;
import com.aish.mvc.entity.ai.AiConversation;
import com.aish.mvc.entity.ai.AiMessage;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.ReportTargetType;
import com.aish.mvc.repository.ai.AiConversationRepository;
import com.aish.mvc.repository.ai.AiMessageRepository;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.service.doc.DocumentAccessPort;
import com.aish.mvc.service.report.ReportService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Quản lý LỊCH SỬ CHAT với AI: danh sách cuộc trò chuyện, tin nhắn trong từng cuộc, đổi tên/xoá,
 * và LƯU LẠI mỗi lượt hỏi-đáp mới (persistExchange). Mỗi cuộc trò chuyện có thể gắn với 1 tài
 * liệu cụ thể (chat về tài liệu đó) hoặc không gắn tài liệu nào (chat chung).
 * Tin nhắn của người dùng còn được quét từ khoá + AI kiểm duyệt ngầm, phát hiện vi phạm thì tự
 * tạo report hệ thống để Admin xem xét.
 */
@Service
@RequiredArgsConstructor
public class AiConversationService {

    private static final Logger log = LoggerFactory.getLogger(AiConversationService.class);

    private final AiConversationRepository aiConversationRepository;
    private final AiMessageRepository aiMessageRepository;
    private final AuthAccountRepository authAccountRepository;
    private final DocumentAccessPort documentAccessPort;
    private final EntityManager entityManager;
    private final ToxicKeywordFilter toxicKeywordFilter;
    private final AiModerationService aiModerationService;
    private final ReportService reportService;

    // Danh sách cuộc trò chuyện của user hiện tại, mới cập nhật gần đây nhất trước.
    @Transactional(readOnly = true)
    public List<AiConversationSummaryDTO> getMyConversations() {
        AuthUser user = requireCurrentUser();
        return aiConversationRepository.findByUser_IdOrderByUpdatedAtDescCreatedAtDesc(user.getId()).stream()
                .map(this::toSummaryDTO)
                .toList();
    }

    // Toàn bộ tin nhắn của một cuộc trò chuyện — chỉ chủ cuộc trò chuyện mới xem được.
    @Transactional(readOnly = true)
    public List<AiMessageDTO> getMyMessages(Long conversationId) {
        AuthUser user = requireCurrentUser();
        requireOwnedConversation(conversationId, user);
        return aiMessageRepository.findByConversation_IdOrderByOrderIndexAsc(conversationId).stream()
                .map(this::toMessageDTO)
                .toList();
    }

    // Lấy N tin nhắn GẦN NHẤT của cuộc trò chuyện — dùng làm "trí nhớ ngắn hạn" khi AI trả lời
    // câu hỏi tiếp theo, không gửi toàn bộ lịch sử (tốn token).
    @Transactional(readOnly = true)
    public List<AiMessage> getRecentMessages(Long conversationId, int limit) {
        if (limit <= 0) return List.of();

        List<AiMessage> messages = aiMessageRepository
                .findByConversation_IdOrderByOrderIndexAsc(conversationId);
        int fromIndex = Math.max(0, messages.size() - limit);
        return List.copyOf(messages.subList(fromIndex, messages.size()));
    }

    // Xoá cuộc trò chuyện — xoá hết tin nhắn bên trong trước, rồi mới xoá cuộc trò chuyện.
    @Transactional
    public void deleteConversation(Long conversationId) {
        AuthUser user = requireCurrentUser();
        AiConversation conversation = requireOwnedConversation(conversationId, user);
        aiMessageRepository.deleteByConversation_Id(conversationId); // xoá bảng ai_messages liên quan
        aiConversationRepository.delete(conversation); // xoá bảng ai_conversations
    }

    // Đổi tên cuộc trò chuyện. Tên không được rỗng.
    @Transactional
    public AiConversationSummaryDTO renameConversation(Long conversationId, String newTitle) {
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation title must not be blank");
        }

        AuthUser user = requireCurrentUser();
        AiConversation conversation = requireOwnedConversation(conversationId, user);
        conversation.setTitle(newTitle.trim());
        conversation.setUpdatedAt(LocalDateTime.now());
        return toSummaryDTO(aiConversationRepository.save(conversation));
    }

    // Tìm cuộc trò chuyện theo id, CHỈ trả về nếu đúng là của user này — tránh xem/sửa/xoá cuộc
    // trò chuyện của người khác.
    @Transactional(readOnly = true)
    public AiConversation requireOwnedConversation(Long conversationId, AuthUser user) {
        return aiConversationRepository.findByIdAndUser_Id(conversationId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }

    // Lưu lại MỘT LƯỢT hỏi-đáp (câu hỏi của user + câu trả lời của AI) vào cuộc trò chuyện.
    // Đầu vào: user, id cuộc trò chuyện (null = tạo cuộc mới), id tài liệu đang chat cùng (có thể
    // null), nội dung câu hỏi và câu trả lời. Trả về: cuộc trò chuyện đã cập nhật.
    // Các bước: (1) chưa có conversationId thì tạo cuộc trò chuyện mới; (2) lưu tin nhắn của
    // user; (3) quét từ khoá nghi vấn -> nếu trúng thì gọi AI kiểm duyệt kỹ hơn, phát hiện vi
    // phạm thì tự tạo report hệ thống cho Admin (lỗi bước này không chặn chat tiếp tục);
    // (4) lưu tin nhắn trả lời của AI; (5) cập nhật thời điểm sửa đổi gần nhất của cuộc trò chuyện.
    @Transactional
    public AiConversation persistExchange(AuthUser user, Long conversationId, Long documentId, String userText, String assistantText) {
        AiConversation conversation = conversationId == null
                ? createConversation(user, documentId, userText)
                : requireOwnedConversation(conversationId, user);

        int nextOrder = aiMessageRepository.countByConversation_Id(conversation.getId());
        AiMessage userMessage = AiMessage.builder()
                .conversation(conversation)
                .role(AiMessage.Role.USER)
                .content(userText)
                .orderIndex(nextOrder)
                .build();
        aiMessageRepository.save(userMessage); // lưu bảng ai_messages: tin nhắn của user

        try {
            // Lọc rẻ trước: chỉ gọi AI kiểm duyệt (tốn tiền/thời gian) khi trúng từ khoá nghi vấn.
            if (toxicKeywordFilter.containsSuspiciousKeyword(userText)) {
                ModerationResultDTO moderationResult = aiModerationService.screenText(userText);
                if (moderationResult.getDecision() == ModerationDecision.FLAG) {
                    // AI xác nhận vi phạm -> tự tạo report hệ thống để Admin xem xét.
                    reportService.createSystemReport(
                            ReportTargetType.AI_MESSAGE,
                            userMessage.getId(),
                            moderationResult.getReason());
                }
            }
        } catch (Exception exception) {
            log.warn("Không thể kiểm duyệt/tạo report hệ thống cho AI message {}; chat vẫn tiếp tục: {}",
                    userMessage.getId(), exception.getMessage(), exception);
        }

        AiMessage assistantMessage = AiMessage.builder()
                .conversation(conversation)
                .role(AiMessage.Role.ASSISTANT)
                .content(assistantText)
                .orderIndex(nextOrder + 1)
                .build();
        aiMessageRepository.save(assistantMessage); // lưu bảng ai_messages: câu trả lời của AI

        conversation.setUpdatedAt(LocalDateTime.now());
        return aiConversationRepository.save(conversation); // lưu bảng ai_conversations
    }

    // Lấy user đang đăng nhập từ token; guest (chưa đăng nhập) thì trả null thay vì báo lỗi.
    public AuthUser currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return authAccountRepository.findByIdentifier(auth.getName())
                .map(a -> a.getUser())
                .orElse(null);
    }

    // Tạo cuộc trò chuyện mới, tự đặt tên từ câu hỏi đầu tiên (rút gọn nếu quá dài).
    private AiConversation createConversation(AuthUser user, Long documentId, String firstMessage) {
        DocDocument document = documentId == null
                ? null
                : entityManager.getReference(DocDocument.class, documentId);

        AiConversation conversation = AiConversation.builder()
                .user(user)
                .document(document)
                .title(titleFrom(firstMessage))
                .build();
        return aiConversationRepository.save(conversation);
    }

    // Giống currentUserOrNull nhưng bắt buộc phải đăng nhập — chưa đăng nhập thì báo lỗi 401.
    private AuthUser requireCurrentUser() {
        AuthUser user = currentUserOrNull();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return user;
    }

    private AiConversationSummaryDTO toSummaryDTO(AiConversation conversation) {
        Long documentId = conversation.getDocument() != null
                ? conversation.getDocument().getId()
                : null;
        String documentTitle = documentId == null
                ? null
                : documentAccessPort.getDocumentTitle(documentId, conversation.getUser().getId()).orElse(null);

        return new AiConversationSummaryDTO(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                documentId,
                documentTitle);
    }

    private AiMessageDTO toMessageDTO(AiMessage message) {
        return new AiMessageDTO(
                message.getId(),
                message.getRole().name(),
                message.getContent(),
                message.getOrderIndex(),
                message.getCreatedAt());
    }

    // Sinh tiêu đề cuộc trò chuyện từ câu hỏi đầu tiên: gộp khoảng trắng, cắt tối đa 50 ký tự.
    private String titleFrom(String text) {
        String normalized = text == null ? "" : text.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) return "Cuộc trò chuyện mới";
        return normalized.length() <= 50 ? normalized : normalized.substring(0, 50);
    }
}
