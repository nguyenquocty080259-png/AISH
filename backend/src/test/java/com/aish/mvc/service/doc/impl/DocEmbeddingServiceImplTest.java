package com.aish.mvc.service.doc.impl;

import com.google.genai.errors.ClientException;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.errors.ServerException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies embedWithRetry() actually retries transient Gemini failures (429/5xx/IO),
 * fails fast on permanent 4xx errors, and gives up after MAX_EMBED_ATTEMPTS.
 */
class DocEmbeddingServiceImplTest {

    // embedWithRetry() chỉ chạm tới embeddingModel — các dependency còn lại của constructor
    // (repository, vector store, transaction manager, system settings) để null cho gọn, chạm
    // vào chúng trong phạm vi test này sẽ NPE ngay chứ không âm thầm dùng hành vi giả.
    private DocEmbeddingServiceImpl newService(EmbeddingModel embeddingModel) {
        return new DocEmbeddingServiceImpl(null, null, null, embeddingModel, null, null, null);
    }

    @Test
    void retriesOnRateLimitThenSucceeds() {
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        float[] expected = {0.1f, 0.2f, 0.3f};
        when(embeddingModel.embed(anyString()))
                .thenThrow(new ClientException(429, "RESOURCE_EXHAUSTED", "rate limited"))
                .thenThrow(new ServerException(503, "UNAVAILABLE", "temporarily down"))
                .thenReturn(expected);

        DocEmbeddingServiceImpl service = newService(embeddingModel);

        float[] result = service.embedWithRetry("hello world", 1L, 0);

        assertArrayEquals(expected, result);
        // 2 lỗi tạm thời + 1 lần thành công = đúng 3 lần gọi -> chứng minh retry thật sự chạy.
        verify(embeddingModel, times(3)).embed(anyString());
    }

    @Test
    void retriesOnTransientIOErrorThenSucceeds() {
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        float[] expected = {0.5f};
        when(embeddingModel.embed(anyString()))
                .thenThrow(new GenAiIOException("connection reset", new java.io.IOException("reset")))
                .thenReturn(expected);

        DocEmbeddingServiceImpl service = newService(embeddingModel);

        float[] result = service.embedWithRetry("hello", 2L, 1);

        assertArrayEquals(expected, result);
        verify(embeddingModel, times(2)).embed(anyString());
    }

    @Test
    void doesNotRetryOnPermanentClientError() {
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        when(embeddingModel.embed(anyString()))
                .thenThrow(new ClientException(400, "INVALID_ARGUMENT", "bad input"));

        DocEmbeddingServiceImpl service = newService(embeddingModel);

        assertThrows(RuntimeException.class, () -> service.embedWithRetry("bad text", 3L, 0));

        // Lỗi 4xx không phải 429 -> KHÔNG được retry, chỉ gọi đúng 1 lần.
        verify(embeddingModel, times(1)).embed(anyString());
    }

    @Test
    void exhaustsRetriesAndFailsClearlyOnPersistentRateLimit() {
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        when(embeddingModel.embed(anyString()))
                .thenThrow(new ClientException(429, "RESOURCE_EXHAUSTED", "still rate limited"));

        DocEmbeddingServiceImpl service = newService(embeddingModel);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> service.embedWithRetry("persistent failure", 4L, 2));

        // Thông báo lỗi phải nêu rõ document/chunk nào fail (không "silently continuing").
        org.junit.jupiter.api.Assertions.assertTrue(thrown.getMessage().contains("4"));
        org.junit.jupiter.api.Assertions.assertTrue(thrown.getMessage().contains("2"));
        // Đúng MAX_EMBED_ATTEMPTS (3) lần thử rồi bỏ cuộc, không lặp vô hạn.
        verify(embeddingModel, times(3)).embed(anyString());
    }
}
