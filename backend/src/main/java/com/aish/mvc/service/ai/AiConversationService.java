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

    @Transactional(readOnly = true)
    public List<AiConversationSummaryDTO> getMyConversations() {
        AuthUser user = requireCurrentUser();
        return aiConversationRepository.findByUser_IdOrderByUpdatedAtDescCreatedAtDesc(user.getId()).stream()
                .map(this::toSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AiMessageDTO> getMyMessages(Long conversationId) {
        AuthUser user = requireCurrentUser();
        requireOwnedConversation(conversationId, user);
        return aiMessageRepository.findByConversation_IdOrderByOrderIndexAsc(conversationId).stream()
                .map(this::toMessageDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AiMessage> getRecentMessages(Long conversationId, int limit) {
        if (limit <= 0) return List.of();

        List<AiMessage> messages = aiMessageRepository
                .findByConversation_IdOrderByOrderIndexAsc(conversationId);
        int fromIndex = Math.max(0, messages.size() - limit);
        return List.copyOf(messages.subList(fromIndex, messages.size()));
    }

    @Transactional
    public void deleteConversation(Long conversationId) {
        AuthUser user = requireCurrentUser();
        AiConversation conversation = requireOwnedConversation(conversationId, user);
        aiMessageRepository.deleteByConversation_Id(conversationId);
        aiConversationRepository.delete(conversation);
    }

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

    @Transactional(readOnly = true)
    public AiConversation requireOwnedConversation(Long conversationId, AuthUser user) {
        return aiConversationRepository.findByIdAndUser_Id(conversationId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }

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
        aiMessageRepository.save(userMessage);

        try {
            if (toxicKeywordFilter.containsSuspiciousKeyword(userText)) {
                ModerationResultDTO moderationResult = aiModerationService.screenText(userText);
                if (moderationResult.getDecision() == ModerationDecision.FLAG) {
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
        aiMessageRepository.save(assistantMessage);

        conversation.setUpdatedAt(LocalDateTime.now());
        return aiConversationRepository.save(conversation);
    }

    public AuthUser currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return authAccountRepository.findByIdentifier(auth.getName())
                .map(a -> a.getUser())
                .orElse(null);
    }

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

    private String titleFrom(String text) {
        String normalized = text == null ? "" : text.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) return "Cuộc trò chuyện mới";
        return normalized.length() <= 50 ? normalized : normalized.substring(0, 50);
    }
}
