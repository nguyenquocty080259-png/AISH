package com.aish.mvc.service.ai;

import com.aish.mvc.entity.auth.AuthUser;
import com.aish.mvc.entity.doc.DocDocument;
import com.aish.mvc.entity.enums.DocumentVisibility;
import com.aish.mvc.entity.enums.ModerationStatus;
import com.aish.mvc.repository.doc.CollectionItemRepository;
import com.aish.mvc.repository.doc.DocDocumentRepository;
import com.aish.mvc.repository.doc.FavoriteRepository;
import com.aish.mvc.repository.report.ReportRepository;
import com.aish.mvc.service.doc.DocumentAccessPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class UserAiToolsTest {
    private AiConversationService conversations;
    private DocDocumentRepository documents;
    private FavoriteRepository favorites;
    private CollectionItemRepository collectionItems;
    private ReportRepository reports;
    private DocumentAccessPort access;
    private UserAiTools tools;

    @BeforeEach
    void setUp() {
        conversations = mock(AiConversationService.class);
        documents = mock(DocDocumentRepository.class);
        favorites = mock(FavoriteRepository.class);
        collectionItems = mock(CollectionItemRepository.class);
        reports = mock(ReportRepository.class);
        access = mock(DocumentAccessPort.class);
        tools = new UserAiTools(conversations, documents, favorites, collectionItems, reports, access);
    }

    @Test
    void everyToolRequiresCurrentSecurityContextUser() {
        when(conversations.currentUserOrNull()).thenReturn(null);

        assertTrue(tools.getMyStats().contains("cần đăng nhập"));
        assertTrue(tools.searchMyDocuments(null, null).contains("cần đăng nhập"));
        assertTrue(tools.getMyDocumentStatus(1L).contains("cần đăng nhập"));
        assertTrue(tools.getMyReportStatus().contains("cần đăng nhập"));
        verifyNoInteractions(documents, favorites, collectionItems, reports, access);
    }

    @Test
    void ownershipRefusalIsIdenticalForMissingAndOtherUsersDocument() {
        AuthUser current = user(10L, "A");
        when(conversations.currentUserOrNull()).thenReturn(current);
        when(documents.findById(77L)).thenReturn(Optional.empty());
        when(documents.findById(88L)).thenReturn(Optional.of(document(88L, "B private", user(20L, "B"))));

        String missing = tools.getMyDocumentStatus(77L);
        String notMine = tools.getMyDocumentStatus(88L);

        assertEquals("Không tìm thấy tài liệu ID 77 trong tài liệu của bạn.", missing);
        assertEquals(missing.replace("77", "88"), notMine);
        assertFalse(notMine.contains("B private"));
    }

    @Test
    void searchDeduplicatesIntoHighestPriorityGroupAndAppliesFinalAccessGate() {
        AuthUser current = user(10L, "Owner A");
        AuthUser other = user(20L, "Owner B");
        DocDocument own = document(1L, "Shared keyword own", current);
        DocDocument favorite = document(2L, "Shared keyword favorite", other);
        DocDocument collection = document(3L, "Shared keyword collection", other);
        DocDocument inaccessiblePublic = document(4L, "Shared keyword blocked", other);
        collection.setVisibility(DocumentVisibility.PUBLIC);
        collection.setModerationStatus(ModerationStatus.APPROVED);
        inaccessiblePublic.setVisibility(DocumentVisibility.PUBLIC);
        inaccessiblePublic.setModerationStatus(ModerationStatus.APPROVED);

        when(conversations.currentUserOrNull()).thenReturn(current);
        when(documents.findByDeletedAtIsNullAndUser_Id(10L)).thenReturn(List.of(own));
        when(favorites.findDocumentIdsByUserId(10L)).thenReturn(List.of(1L, 2L));
        when(documents.findAllById(List.of(1L, 2L))).thenReturn(List.of(own, favorite));
        when(collectionItems.findDocumentIdsByCollectionOwner(10L)).thenReturn(List.of(2L, 3L));
        when(documents.findAllById(List.of(2L, 3L))).thenReturn(List.of(favorite, collection));
        when(documents.findPublicApprovedDocuments(DocumentVisibility.PUBLIC, ModerationStatus.APPROVED))
                .thenReturn(List.of(collection, inaccessiblePublic));
        when(access.isAvailableTo(anyLong(), eq(10L))).thenReturn(true);
        when(access.isAvailableTo(4L, 10L)).thenReturn(false);

        String result = tools.searchMyDocuments("  KEYword ", 10);

        assertTrue(result.contains("ID 1") && result.contains("Nhóm: Tài liệu của tôi"));
        assertTrue(result.contains("ID 2") && result.contains("Nhóm: Yêu thích"));
        assertTrue(result.contains("ID 3") && result.contains("Nhóm: Trong collection"));
        assertEquals(1, occurrences(result, "ID 2"));
        assertEquals(1, occurrences(result, "ID 3"));
        assertFalse(result.contains("ID 4"));
        verify(access).isAvailableTo(4L, 10L);
    }

    @Test
    void repositoryFailureReturnsFriendlyText() {
        when(conversations.currentUserOrNull()).thenReturn(user(10L, "A"));
        when(documents.findByDeletedAtIsNullAndUser_Id(10L))
                .thenThrow(new RuntimeException("database unavailable"));

        assertTrue(tools.getMyStats().startsWith("Không thể lấy thống kê cá nhân"));
    }

    private static AuthUser user(Long id, String name) {
        AuthUser user = new AuthUser();
        user.setId(id);
        user.setFullName(name);
        return user;
    }

    private static DocDocument document(Long id, String title, AuthUser owner) {
        return DocDocument.builder().id(id).title(title).user(owner)
                .visibility(DocumentVisibility.PRIVATE)
                .moderationStatus(ModerationStatus.NOT_REQUIRED).build();
    }

    private static int occurrences(String value, String needle) {
        return (value.length() - value.replace(needle, "").length()) / needle.length();
    }
}
