package com.aish.mvc.service.ai;

import com.aish.mvc.entity.ai.AiModel;
import com.aish.mvc.entity.ai.AiUsageLog;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.repository.ai.AiModelRepository;
import com.aish.mvc.repository.ai.AiUsageLogRepository;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * GHI LẠI mỗi lần gọi AI (chat, kiểm duyệt, gợi ý metadata...) vào bảng ai_usage_logs: số token
 * dùng, chi phí ước tính, ai gọi. Dữ liệu này nuôi trang thống kê AI cho Admin
 * ({@link AiUsageStatsService}). Việc ghi log không bao giờ làm hỏng luồng chính — lỗi khi ghi
 * chỉ log cảnh báo, không ném ra ngoài.
 */
@Service
@RequiredArgsConstructor
public class AiUsageTracker {
    private static final Logger log = LoggerFactory.getLogger(AiUsageTracker.class);

    private final AiUsageLogRepository usageLogRepository;
    private final AiModelRepository aiModelRepository;
    private final AuthAccountRepository authAccountRepository;
    private final PlatformTransactionManager transactionManager;

    @Value("${spring.ai.openai.chat.options.model}")
    private String configuredChatModel;

    // Ghi một lượt gọi AI. Đầu vào: loại lệnh gọi (vd "AI_CHAT", "DOC_MODERATION"), phản hồi từ
    // AI (để đọc số token dùng), và id tin nhắn liên quan (có thể null). Không trả về gì.
    // Dùng giao dịch RIÊNG (REQUIRES_NEW) để việc ghi log không bị cuốn theo rollback của giao
    // dịch chính, và ngược lại lỗi ghi log không làm rollback giao dịch chính.
    public void log(String callType, ChatResponse chatResponse, Long messageId) {
        try {
            AiModel model = aiModelRepository.findByModelKey(configuredChatModel)
                    .or(() -> aiModelRepository.findFirstByIsActiveTrueOrderByIdAsc())
                    .orElse(null);
            if (model == null) {
                log.warn("Skipping AI usage log for {} because no active AI model exists", callType);
                return;
            }

            Integer inputTokens = null;
            Integer outputTokens = null;
            if (chatResponse != null && chatResponse.getMetadata() != null) {
                Usage usage = chatResponse.getMetadata().getUsage();
                if (usage != null) {
                    inputTokens = positiveOrNull(usage.getPromptTokens());
                    outputTokens = positiveOrNull(usage.getCompletionTokens());
                }
            }
            Integer totalTokens = inputTokens == null && outputTokens == null
                    ? null : valueOrZero(inputTokens) + valueOrZero(outputTokens);
            AuthUser currentUser = currentUserOrNull();

            AiUsageLog usageLog = AiUsageLog.builder()
                    .messageId(messageId)
                    .callType(callType)
                    .userId(currentUser == null ? null : currentUser.getId())
                    .model(model)
                    .inputTokens(inputTokens)
                    .outputTokens(outputTokens)
                    .totalTokens(totalTokens)
                    .costUsd(calculateCost(inputTokens, outputTokens,
                            model.getInputPricePer1m(), model.getOutputPricePer1m()))
                    .build();
            TransactionTemplate transaction = new TransactionTemplate(transactionManager);
            transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            transaction.executeWithoutResult(status -> usageLogRepository.save(usageLog)); // lưu bảng ai_usage_logs
        } catch (Exception exception) {
            log.warn("Could not persist AI usage for {}; AI response remains available: {}",
                    callType, exception.getMessage());
        }
    }

    // Ai đang gọi AI — lấy từ token đăng nhập. Chưa đăng nhập (guest) thì trả null.
    private AuthUser currentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) return null;
        return authAccountRepository.findByIdentifier(authentication.getName())
                .map(account -> account.getUser()).orElse(null);
    }

    // Ước tính chi phí (USD) dựa trên đơn giá mỗi 1 triệu token của model đang dùng.
    static Double calculateCost(Integer inputTokens, Integer outputTokens,
                                Double inputPricePer1m, Double outputPricePer1m) {
        if (inputPricePer1m == null || outputPricePer1m == null) return null;
        return (valueOrZero(inputTokens) * inputPricePer1m
                + valueOrZero(outputTokens) * outputPricePer1m) / 1_000_000D;
    }

    private static Integer positiveOrNull(Integer value) {
        return value == null || value <= 0 ? null : value;
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
