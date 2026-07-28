package com.aish.mvc.service.doc.impl;

import com.aish.mvc.entity.auth.AuthAccount;
import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.doc.Rating;
import com.aish.mvc.repository.auth.AuthAccountRepository;
import com.aish.mvc.repository.doc.CommentRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.DownloadRepository;
import com.aish.mvc.repository.doc.FavoriteRepository;
import com.aish.mvc.repository.doc.RatingRepository;
import com.aish.mvc.repository.doc.ViewHistoryRepository;
import com.aish.mvc.service.ai.ToxicKeywordFilter;
import com.aish.mvc.service.doc.DocumentAccessPort;
import com.aish.mvc.service.notification.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * rateDocument() phải tự chốt thang điểm 1-5 ở BE: FE (RatingStars) chỉ gửi 1-5 nhưng API gọi
 * trực tiếp được, và một điểm rác đi lọt sẽ kéo lệch averageRating của tài liệu lẫn bộ lọc
 * "điểm tối thiểu" của trang Cộng đồng — sai lệch âm thầm, không ai thấy cho tới khi so số.
 */
class EngagementServiceImplRatingTest {

    private static final String CURRENT_USER_EMAIL = "me@example.com";
    private static final Long CURRENT_USER_ID = 7L;
    private static final Long DOCUMENT_ID = 3L;

    private final DocDocumentRepository docDocumentRepository = mock(DocDocumentRepository.class);
    private final RatingRepository ratingRepository = mock(RatingRepository.class);
    private final AuthAccountRepository authAccountRepository = mock(AuthAccountRepository.class);
    private final DocumentAccessPort documentAccessPort = mock(DocumentAccessPort.class);

    private EngagementServiceImpl service;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(CURRENT_USER_EMAIL, "n/a"));

        AuthUser authUser = new AuthUser();
        authUser.setId(CURRENT_USER_ID);
        AuthAccount authAccount = new AuthAccount();
        authAccount.setUser(authUser);
        when(authAccountRepository.findByIdentifier(CURRENT_USER_EMAIL)).thenReturn(Optional.of(authAccount));

        service = new EngagementServiceImpl(
                docDocumentRepository,
                mock(CommentRepository.class),
                mock(FavoriteRepository.class),
                ratingRepository,
                mock(DownloadRepository.class),
                mock(ViewHistoryRepository.class),
                authAccountRepository,
                mock(ToxicKeywordFilter.class),
                mock(NotificationService.class),
                mock(ApplicationEventPublisher.class),
                documentAccessPort);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void givenExistingDocument() {
        when(docDocumentRepository.findById(DOCUMENT_ID))
                .thenReturn(Optional.of(DocDocument.builder().id(DOCUMENT_ID).build()));
        when(ratingRepository.findByUserIdAndDocument_Id(CURRENT_USER_ID, DOCUMENT_ID))
                .thenReturn(Optional.empty());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, 6, 999 })
    void rejectsStarOutsideOneToFive(int star) {
        assertThrows(IllegalArgumentException.class, () -> service.rateDocument(DOCUMENT_ID, star));

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void rejectsNullStar() {
        assertThrows(IllegalArgumentException.class, () -> service.rateDocument(DOCUMENT_ID, null));

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void rejectsInvalidStarBeforeTouchingTheDatabase() {
        // Chặn ngay đầu method: request rác không được phép tạo cả truy vấn tài liệu.
        assertThrows(IllegalArgumentException.class, () -> service.rateDocument(DOCUMENT_ID, 42));

        verify(docDocumentRepository, never()).findById(DOCUMENT_ID);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 3, 5 })
    void acceptsStarInsideOneToFiveIncludingBounds(int star) {
        givenExistingDocument();

        service.rateDocument(DOCUMENT_ID, star);

        ArgumentCaptor<Rating> saved = ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository).save(saved.capture());
        assertEquals(star, saved.getValue().getRating());
    }
}
