import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { useLocation } from "react-router-dom";
import i18n from "../i18n";
import * as aiChatApi from "../api/aiChatApi";
import * as documentApi from "../api/documentApi";
import { useAuth } from "../hooks/useAuth";

const AiWidgetContext = createContext(null);

// Tin nhắn chào mở đầu mỗi cuộc trò chuyện mới (chưa có lịch sử).
const createGreeting = () => ({
  id: "ai-greeting",
  role: "ai",
  text: i18n.t("aiWidget.greeting"),
});

// Chuyển tin nhắn lấy từ lịch sử (BE) sang định dạng hiển thị trong widget.
const mapHistoryMessage = (message) => ({
  id: `history-${message.id}`,
  role: message.role === "USER" ? "user" : "ai",
  text: message.content,
});

// Đang ở trang chi tiết tài liệu (/documents/{id}) thì lấy ra id đó để tự bật "hỏi AI về tài liệu này".
function getDocumentIdFromPath(pathname) {
  const match = pathname.match(/^\/documents\/(\d+)$/);
  return match ? Number(match[1]) : null;
}

/**
 * Trạng thái CHUNG cho WIDGET CHAT AI nổi (hiển thị ở mọi trang): mở/đóng, tin nhắn, đang gõ,
 * lịch sử cuộc trò chuyện, và ngữ cảnh tài liệu đang xem (để tự bật chế độ RAG khi đứng ở trang
 * chi tiết tài liệu). Bọc quanh toàn app để widget giữ được trạng thái khi chuyển trang.
 */
export function AiWidgetProvider({ children }) {
  const location = useLocation();
  const { isAuthenticated } = useAuth();
  const routeDocumentId = getDocumentIdFromPath(location.pathname);

  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([createGreeting()]);
  const [isTyping, setIsTyping] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [conversationId, setConversationId] = useState(null);
  const [conversations, setConversations] = useState([]);
  const [historyLoaded, setHistoryLoaded] = useState(false);
  const [isHistoryLoading, setIsHistoryLoading] = useState(false);
  const [isHistoryPanelOpen, setIsHistoryPanelOpen] = useState(false);
  const [docContextEnabled, setDocContextEnabled] = useState(Boolean(routeDocumentId));
  const [docCache, setDocCache] = useState({});
  const isOpenRef = useRef(isOpen);

  useEffect(() => {
    isOpenRef.current = isOpen;
  }, [isOpen]);

  useEffect(() => {
    setDocContextEnabled(Boolean(routeDocumentId));
  }, [routeDocumentId]);

  const currentDoc = routeDocumentId ? docCache[routeDocumentId] : null;
  const currentDocTitle = currentDoc?.title || i18n.t("aiWidget.docContextDefault");
  const currentDocReady =
    Boolean(routeDocumentId) &&
    currentDoc?.aiSupported !== false &&
    currentDoc?.ingestStatus === "INGESTED";

  useEffect(() => {
    if (routeDocumentId && currentDoc && !currentDocReady) {
      setDocContextEnabled(false);
    }
  }, [currentDoc, currentDocReady, routeDocumentId]);

  useEffect(() => {
    if (!routeDocumentId || !isOpen || docCache[routeDocumentId]) return;

    let cancelled = false;
    documentApi
      .getOne(routeDocumentId)
      .then((doc) => {
        if (cancelled) return;
        setDocCache((prev) => ({
          ...prev,
          [routeDocumentId]: doc || { title: i18n.t("aiWidget.docContextDefault") },
        }));
      })
      .catch(() => {
        if (cancelled) return;
        setDocCache((prev) => ({
          ...prev,
          [routeDocumentId]: { title: i18n.t("aiWidget.docContextDefault"), aiSupported: false },
        }));
      });

    return () => {
      cancelled = true;
    };
  }, [docCache, isOpen, routeDocumentId]);

  // Gọi API lấy danh sách cuộc trò chuyện của user hiện tại (guest thì bỏ qua).
  const loadConversations = useCallback(async () => {
    if (!isAuthenticated) return [];

    const data = await aiChatApi.getConversations();
    const nextConversations = Array.isArray(data) ? data : [];
    setConversations(nextConversations);
    return nextConversations;
  }, [isAuthenticated]);

  // Gọi API nạp toàn bộ tin nhắn của một cuộc trò chuyện cụ thể, thay thế nội dung widget.
  const loadConversationMessages = useCallback(
    async (nextConversationId) => {
      if (!isAuthenticated || !nextConversationId) return;

      setIsHistoryLoading(true);
      try {
        const history = await aiChatApi.getMessages(nextConversationId);
        const mappedMessages = Array.isArray(history)
          ? history.map(mapHistoryMessage)
          : [];

        setConversationId(nextConversationId);
        setMessages(mappedMessages.length > 0 ? mappedMessages : [createGreeting()]);
      } catch (error) {
        console.error("Failed to load AI HiveMind conversation", error);
      } finally {
        setIsHistoryLoading(false);
      }
    },
    [isAuthenticated]
  );

  // Mở widget và xoá số tin nhắn chưa đọc (coi như đã xem).
  const openWidget = () => {
    setIsOpen(true);
    setUnreadCount(0);
  };

  const closeWidget = () => {
    setIsOpen(false);
    setIsHistoryPanelOpen(false);
  };

  // Bật/tắt bảng lịch sử cuộc trò chuyện; mở lần đầu thì nạp danh sách cuộc trò chuyện.
  const toggleHistoryPanel = async () => {
    if (!isAuthenticated) return;

    setIsHistoryPanelOpen((current) => !current);
    if (!isHistoryPanelOpen) {
      try {
        await loadConversations();
      } catch (error) {
        console.error("Failed to load AI HiveMind conversations", error);
      }
    }
  };

  // Bắt đầu cuộc trò chuyện mới: xoá conversationId hiện tại, chỉ còn lời chào.
  const startNewConversation = () => {
    setConversationId(null);
    setMessages([createGreeting()]);
    setIsHistoryPanelOpen(false);
  };

  // Chọn một cuộc trò chuyện cũ từ bảng lịch sử để xem tiếp.
  const selectConversation = async (nextConversationId) => {
    if (!isAuthenticated || !nextConversationId) return;

    setIsHistoryPanelOpen(false);
    await loadConversationMessages(nextConversationId);
  };

  useEffect(() => {
    if (!isAuthenticated) {
      setHistoryLoaded(false);
      setIsHistoryLoading(false);
      setConversationId(null);
      setConversations([]);
      setIsHistoryPanelOpen(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    const shouldLoadHistory =
      isOpen &&
      isAuthenticated &&
      !historyLoaded &&
      !conversationId &&
      messages.length === 1 &&
      messages[0]?.id === "ai-greeting";

    if (!shouldLoadHistory) return;

    let cancelled = false;
    setIsHistoryLoading(true);

    async function loadLatestConversation() {
      try {
        const nextConversations = await loadConversations();
        const latest = nextConversations[0];
        if (!latest?.id || cancelled) {
          setHistoryLoaded(true);
          return;
        }

        const history = await aiChatApi.getMessages(latest.id);
        if (cancelled) return;

        const mappedMessages = Array.isArray(history)
          ? history.map(mapHistoryMessage)
          : [];

        setConversationId(latest.id);
        setMessages(mappedMessages.length > 0 ? mappedMessages : [createGreeting()]);
      } catch (error) {
        console.error("Failed to load AI HiveMind history", error);
      } finally {
        if (!cancelled) {
          setHistoryLoaded(true);
          setIsHistoryLoading(false);
        }
      }
    }

    loadLatestConversation();

    return () => {
      cancelled = true;
      setIsHistoryLoading(false);
    };
  }, [conversationId, historyLoaded, isAuthenticated, isOpen, loadConversations, messages]);

  // Gửi tin nhắn mới cho AI. Đầu vào: nội dung câu hỏi. Các bước: (1) thêm tin nhắn của user vào
  // giao diện ngay (optimistic UI); (2) gọi API /ai/chat, kèm documentId nếu đang bật ngữ cảnh
  // tài liệu; (3) thêm câu trả lời của AI vào giao diện; (4) đã đăng nhập thì nạp lại danh sách
  // cuộc trò chuyện (để cập nhật tiêu đề/thời gian mới nhất); (5) lỗi thì hiện tin nhắn báo lỗi.
  const sendMessage = async (text) => {
    const cleanText = text.trim();
    if (!cleanText || isTyping || isHistoryLoading) return;

    const userMessage = {
      id: `user-${Date.now()}`,
      role: "user",
      text: cleanText,
    };

    const documentIdForRequest =
      routeDocumentId && docContextEnabled && currentDocReady
        ? routeDocumentId
        : null;

    setMessages((prev) => [...prev, userMessage]);
    setIsTyping(true);

    try {
      const response = await aiChatApi.chat({
        message: cleanText,
        documentId: documentIdForRequest,
        conversationId,
      });

      const nextConversationId = response.conversationId || conversationId;
      if (nextConversationId) {
        setConversationId(nextConversationId);
      }

      const aiMessage = {
        id: `ai-${Date.now()}`,
        role: "ai",
        text: response.answer || i18n.t("aiWidget.noAnswer"),
        mode: response.mode,
      };

      setMessages((prev) => [...prev, aiMessage]);
      setUnreadCount((count) => (isOpenRef.current ? 0 : count + 1));

      if (isAuthenticated && nextConversationId) {
        try {
          await loadConversations();
        } catch (error) {
          console.error("Failed to refresh AI HiveMind conversations", error);
        }
      }
    } catch {
      const errorMessage = {
        id: `ai-error-${Date.now()}`,
        role: "ai",
        text: i18n.t("aiWidget.error"),
        isError: true,
      };

      setMessages((prev) => [...prev, errorMessage]);
      setUnreadCount((count) => (isOpenRef.current ? 0 : count + 1));
    } finally {
      setIsTyping(false);
    }
  };

  const value = useMemo(
    () => ({
      isOpen,
      messages,
      isTyping,
      isHistoryLoading,
      isHistoryPanelOpen,
      unreadCount,
      conversationId,
      conversations,
      isAuthenticated,
      routeDocumentId,
      currentDocTitle,
      currentDocReady,
      currentDocIngestStatus: currentDoc?.ingestStatus || null,
      currentDocAiSupported: currentDoc?.aiSupported ?? null,
      docContextEnabled,
      openWidget,
      closeWidget,
      sendMessage,
      toggleHistoryPanel,
      startNewConversation,
      selectConversation,
      setDocContextEnabled,
    }),
    [
      conversationId,
      conversations,
      currentDocTitle,
      currentDocReady,
      currentDoc,
      docContextEnabled,
      isAuthenticated,
      isHistoryLoading,
      isHistoryPanelOpen,
      isOpen,
      isTyping,
      messages,
      routeDocumentId,
      unreadCount,
    ]
  );

  return <AiWidgetContext.Provider value={value}>{children}</AiWidgetContext.Provider>;
}

// Hook tiện lợi để đọc/điều khiển trạng thái widget chat AI từ bất kỳ component nào.
export function useAiWidget() {
  const context = useContext(AiWidgetContext);
  if (!context) {
    throw new Error("useAiWidget must be used inside <AiWidgetProvider>");
  }
  return context;
}
