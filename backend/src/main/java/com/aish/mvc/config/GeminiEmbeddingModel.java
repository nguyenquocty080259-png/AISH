package com.aish.mvc.config;

import com.google.genai.Client;
import com.google.genai.types.EmbedContentConfig;
import com.google.genai.types.EmbedContentResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.ArrayList;
import java.util.List;

/**
 * spring-ai-google-genai 2.0.0-M3 ships GoogleGenAiChatModel but no embedding
 * classes (GoogleGenAiEmbeddingConnectionDetails / GoogleGenAiTextEmbeddingModel
 * referenced by spring-ai-autoconfigure-model-google-genai simply aren't in the
 * jar for this milestone), so the built-in embedding autoconfiguration can never
 * produce a bean. This wraps the same com.google.genai.Client used for chat
 * directly against the Gemini Developer API's embedContent call instead.
 */
public class GeminiEmbeddingModel implements EmbeddingModel {

    private final Client client;
    private final String model;
    private final Integer outputDimensionality;

    public GeminiEmbeddingModel(Client client, String model, Integer outputDimensionality) {
        this.client = client;
        this.model = model;
        this.outputDimensionality = outputDimensionality;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<String> inputs = request.getInstructions();
        List<Embedding> embeddings = new ArrayList<>(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            embeddings.add(new Embedding(embedText(inputs.get(i)), i));
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return embedText(document.getText());
    }

    private float[] embedText(String text) {
        EmbedContentConfig.Builder configBuilder = EmbedContentConfig.builder();
        if (outputDimensionality != null) {
            configBuilder.outputDimensionality(outputDimensionality);
        }
        EmbedContentResponse response = client.models.embedContent(model, text, configBuilder.build());
        List<Float> values = response.embeddings()
                .orElseThrow(() -> new IllegalStateException("Gemini embedContent returned no embeddings"))
                .get(0)
                .values()
                .orElseThrow(() -> new IllegalStateException("Gemini embedContent returned empty vector values"));

        float[] result = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }
}
