package com.aish.mvc.config;

import com.google.genai.Client;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(@Qualifier("openAiChatModel") ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    // Manually built: spring-ai-google-genai 2.0.0-M3 has no embedding classes
    // (only chat), so the library's own embedding autoconfiguration can never
    // produce a bean — see GeminiEmbeddingModel for details. This client is
    // independent of the Groq ChatModel above; OpenAI/Groq has no embeddings
    // endpoint, so embedding is a separate provider from chat.
    @Bean
    public EmbeddingModel embeddingModel(
            @Value("${spring.ai.google.genai.embedding.api-key}") String geminiApiKey,
            @Value("${spring.ai.google.genai.embedding.text.options.model}") String geminiEmbeddingModel,
            @Value("${spring.ai.google.genai.embedding.text.options.dimensions:768}") Integer geminiEmbeddingDimensions) {
        Client geminiClient = Client.builder().apiKey(geminiApiKey).build();
        return new GeminiEmbeddingModel(geminiClient, geminiEmbeddingModel, geminiEmbeddingDimensions);
    }

    @Bean
    public HydratableSimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return HydratableSimpleVectorStore.create(embeddingModel);
    }
}