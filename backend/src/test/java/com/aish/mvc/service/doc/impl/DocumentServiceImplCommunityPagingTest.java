package com.aish.mvc.service.doc.impl;

import com.aish.mvc.dto.doc.CommunityPageResponseDTO;
import com.aish.mvc.dto.doc.DocumentResponseDTO;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Phân trang trang Cộng đồng chạy trong bộ nhớ: toàn bộ tài liệu PUBLIC được nạp và map trước
 * khi cắt trang, và mapper còn đếm favorite/download/rating + tải bình luận cho từng tài liệu.
 * Vì vậy size do client gửi phải bị kẹp lại — không có trần thì ?size=1000000 là một request rẻ
 * tiền kéo theo hàng nghìn truy vấn.
 */
class DocumentServiceImplCommunityPagingTest {

    private final DocDocumentRepository docDocumentRepository = mock(DocDocumentRepository.class);
    private final DocumentMapper documentMapper = mock(DocumentMapper.class);

    private DocumentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DocumentServiceImpl();
        ReflectionTestUtils.setField(service, "docDocumentRepository", docDocumentRepository);
        ReflectionTestUtils.setField(service, "documentMapper", documentMapper);
    }

    private void givenPublicDocuments(int count) {
        List<DocDocument> documents = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            documents.add(DocDocument.builder().id(i).build());
        }
        when(docDocumentRepository.findCommunityDocuments(
                eq(DocumentVisibility.PUBLIC), isNull(), isNull())).thenReturn(documents);
        when(documentMapper.toResponseDTO(any(DocDocument.class))).thenAnswer(call -> {
            DocumentResponseDTO dto = new DocumentResponseDTO();
            dto.setId(((DocDocument) call.getArgument(0)).getId());
            return dto;
        });
    }

    @Test
    void oversizedPageRequestIsClampedToTheMaximum() {
        givenPublicDocuments(DocumentServiceImpl.MAX_COMMUNITY_PAGE_SIZE + 20);

        CommunityPageResponseDTO response =
                service.getCommunityDocuments(null, null, null, null, 0, 1_000_000);

        assertEquals(DocumentServiceImpl.MAX_COMMUNITY_PAGE_SIZE, response.getSize());
        assertEquals(DocumentServiceImpl.MAX_COMMUNITY_PAGE_SIZE, response.getItems().size());
    }

    @Test
    void nonPositiveSizeFallsBackToTheDefault() {
        givenPublicDocuments(30);

        CommunityPageResponseDTO response =
                service.getCommunityDocuments(null, null, null, null, 0, 0);

        assertEquals(DocumentServiceImpl.DEFAULT_COMMUNITY_PAGE_SIZE, response.getSize());
        assertEquals(DocumentServiceImpl.DEFAULT_COMMUNITY_PAGE_SIZE, response.getItems().size());
    }

    @Test
    void sizeUnderTheMaximumIsKeptAsRequested() {
        givenPublicDocuments(30);

        CommunityPageResponseDTO response =
                service.getCommunityDocuments(null, null, null, null, 0, 12);

        assertEquals(12, response.getSize());
        assertEquals(12, response.getItems().size());
        assertEquals(30, response.getTotalItems());
    }

    @Test
    void negativePageFallsBackToTheFirstPage() {
        givenPublicDocuments(30);

        CommunityPageResponseDTO response =
                service.getCommunityDocuments(null, null, null, null, -5, 12);

        assertEquals(0, response.getPage());
    }
}
