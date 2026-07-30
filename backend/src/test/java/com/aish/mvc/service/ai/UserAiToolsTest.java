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
        assertTrue(tools.getMyDocumentStatus("đề cương").contains("cần đăng nhập"));
        assertTrue(tools.getMyReportStatus().contains("cần đăng nhập"));
        verifyNoInteractions(documents, favorites, collectionItems, reports, access);
    }

    /**
     * getMyDocumentStatus() tra cứu theo tiêu đề/từ khoá và CHỈ được nhìn vào kho tài liệu của
     * chính người đang đăng nhập. Tài liệu của người khác vì thế phải bị từ chối y hệt như một
     * tài liệu không tồn tại: cùng một câu trả lời, không lộ tiêu đề, và tuyệt đối không có
     * truy vấn nào rộng hơn phạm vi chủ sở hữu.
     */
    @Test
    void ownershipRefusalIsIdenticalForMissingAndOtherUsersDocument() {
        AuthUser current = user(10L, "A");
        when(conversations.currentUserOrNull()).thenReturn(current);
        // Người khác có tài liệu tên "Bí mật nội bộ của B" — nó không nằm trong kho của A nên
        // truy vấn owner-scoped duy nhất mà tool được phép dùng sẽ không bao giờ trả về nó.
        when(documents.findByDeletedAtIsNullAndUser_Id(10L))
                .thenReturn(List.of(document(1L, "Đề cương của A", current)));

        String missing = tools.getMyDocumentStatus("khong-ton-tai");
        String notMine = tools.getMyDocumentStatus("nội bộ");

        assertEquals("Không tìm thấy tài liệu nào của bạn khớp với \"khong-ton-tai\".", missing);
        // Hai lời từ chối chỉ khác đúng từ khoá người dùng gõ vào — không suy ra được tài liệu
        // của người khác có tồn tại hay không.
        assertEquals(missing.replace("khong-ton-tai", "nội bộ"), notMine);
        assertFalse(notMine.contains("Bí mật nội bộ của B"));
        verify(documents, times(2)).findByDeletedAtIsNullAndUser_Id(10L);
        verifyNoMoreInteractions(documents);
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

        // Mỗi tài liệu xuất hiện đúng 1 lần, ở nhóm ưu tiên CAO NHẤT mà nó thuộc về: id 2 vừa
        // được yêu thích vừa nằm trong collection -> chỉ hiện ở "Yêu thích"; id 3 vừa trong
        // collection vừa là PUBLIC/APPROVED -> chỉ hiện ở "Trong collection".
        assertTrue(result.contains("Shared keyword own | Chủ sở hữu: Owner A | Nhóm: Tài liệu của tôi"));
        assertTrue(result.contains("Shared keyword favorite | Chủ sở hữu: Owner B | Nhóm: Yêu thích"));
        assertTrue(result.contains("Shared keyword collection | Chủ sở hữu: Owner B | Nhóm: Trong collection"));
        assertEquals(1, occurrences(result, "Shared keyword favorite"));
        assertEquals(1, occurrences(result, "Shared keyword collection"));
        // Chốt chặn cuối: PUBLIC/APPROVED nhưng DocumentAccessPort từ chối -> không được lọt ra.
        assertFalse(result.contains("Shared keyword blocked"));
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
