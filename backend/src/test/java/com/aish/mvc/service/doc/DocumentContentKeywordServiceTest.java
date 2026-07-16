package com.aish.mvc.service.doc;

import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.DocEmbedding;
import com.aish.mvc.entity.enums.ModerationKeywordType;
import com.aish.mvc.repository.doc.DocEmbeddingRepository;
import com.aish.mvc.service.ai.ToxicKeywordFilter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocumentContentKeywordServiceTest {

    @Test
    void checksExtractedChunksWithDocumentContentType() {
        DocEmbeddingRepository repository = mock(DocEmbeddingRepository.class);
        ToxicKeywordFilter filter = mock(ToxicKeywordFilter.class);
        DocDocument document = new DocDocument();
        document.setId(7L);
        DocEmbedding chunk = DocEmbedding.builder().chunkText("contains xyzt-doc here").build();
        when(repository.findByDocument_IdOrderByChunkIndexAsc(7L)).thenReturn(List.of(chunk));
        when(filter.matches("contains xyzt-doc here", ModerationKeywordType.DOCUMENT_CONTENT))
                .thenReturn(true);
        DocumentContentKeywordService service = new DocumentContentKeywordService(repository, filter);

        assertTrue(service.matches(document));
    }

    @Test
    void chunkInfrastructureFailureIsFailOpen() {
        DocEmbeddingRepository repository = mock(DocEmbeddingRepository.class);
        DocDocument document = new DocDocument();
        document.setId(7L);
        when(repository.findByDocument_IdOrderByChunkIndexAsc(7L))
                .thenThrow(new RuntimeException("database down"));
        DocumentContentKeywordService service = new DocumentContentKeywordService(
                repository, mock(ToxicKeywordFilter.class));

        assertFalse(service.matches(document));
    }
}
