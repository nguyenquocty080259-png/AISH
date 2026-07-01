package com.aish.mvc.service.ai;

import com.aish.mvc.dto.ai.AiChatRequest;
import com.aish.mvc.dto.ai.AiChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final ChatClient chatClient;

    // Thông tin hệ thống — AI dùng để trả lời câu hỏi về website
    private static final String SYSTEM_PROMPT = """
            Bạn là trợ lý AI của AISH — nền tảng học tập thông minh dành cho sinh viên.
            
            Thông tin hệ thống AISH:
            - AISH là nền tảng hỗ trợ học tập bằng AI
            - Người dùng có thể upload tài liệu PDF và đặt câu hỏi về nội dung tài liệu
            - Hỗ trợ chat AI thông minh, tìm kiếm tài liệu, và quản lý tài liệu cá nhân
            - Tài liệu có thể để PUBLIC (mọi người xem) hoặc PRIVATE (chỉ mình xem)
            - Được xây dựng bởi nhóm 6 SWP391 SE1901 SU26
            
            Nguyên tắc trả lời:
            - Nếu câu hỏi liên quan đến AISH → trả lời dựa trên thông tin hệ thống trên
            - Nếu không liên quan → trả lời như AI thông thường
            - Luôn trả lời thân thiện, ngắn gọn, bằng tiếng Việt
            """;

    /**
     * Xử lý câu hỏi theo 3 mode:
     * - RAG:     có documentId → tìm chunk → trả lời theo tài liệu
     * - SYSTEM:  không có documentId → trả lời theo thông tin hệ thống
     * - GENERAL: câu hỏi thông thường (Gemini tự xử lý trong system prompt)
     */
    public AiChatResponse chat(AiChatRequest request) {
        if (request.getDocumentId() != null) {
            return chatWithDocument(request);
        }
        return chatWithSystem(request.getMessage());
    }

    // Mode SYSTEM / GENERAL
    private AiChatResponse chatWithSystem(String userMessage) {
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(SYSTEM_PROMPT),
                new UserMessage(userMessage)
        ));

        String response = chatClient.prompt(prompt)
                .call()
                .content();

        return new AiChatResponse(response, "SYSTEM");
    }

    // Mode RAG — documentId có → tìm chunk liên quan
    // DocEmbeddingService sẽ được inject sau khi implement
    private AiChatResponse chatWithDocument(AiChatRequest request) {
        // TODO: implement sau khi có DocEmbeddingService
        // 1. Embed câu hỏi → vector
        // 2. Tìm top 3 chunk trong document
        // 3. Ghép context + câu hỏi → gọi Gemini

        // Tạm thời fallback về SYSTEM mode
        return chatWithSystem(request.getMessage());
    }
}