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
import * as aiChatApi from "../api/aiChatApi";
import * as documentApi from "../api/documentApi";
import { useAuth } from "../hooks/useAuth";

const AiWidgetContext = createContext(null);

const createGreeting = () => ({
  id: "ai-greeting",
  role: "ai",
  text: "Xin chào  Tôi có thể giúp gì cho bạn?",
});

const mapHistoryMessage = (message) => ({
  id: `history-${message.id}`,
  role: message.role === "USER" ? "user" : "ai",
  text: message.content,
});

function getDocumentIdFromPath(pathname) {
  const match = pathname.match(/^\/documents\/(\d+)$/);
  return match ? Number(match[1]) : null;
}

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
  const [docTitleCache, setDocTitleCache] = useState({});
  const isOpenRef = useRef(isOpen);

  useEffect(() => {
    isOpenRef.current = isOpen;
  }, [isOpen]);

  useEffect(() => {
    setDocContextEnabled(Boolean(routeDocumentId));
  }, [routeDocumentId]);

  useEffect(() => {
    if (!routeDocumentId || !isOpen || docTitleCache[routeDocumentId]) return;

    let cancelled = false;
    documentApi
      .getOne(routeDocumentId)
      .then((doc) => {
        if (cancelled) return;
        setDocTitleCache((prev) => ({
          ...prev,
          [routeDocumentId]: doc?.title || "tài liệu này",
        }));
      })
      .catch(() => {
        if (cancelled) return;
        setDocTitleCache((prev) => ({
          ...prev,
          [routeDocumentId]: "tài liệu này",
        }));
      });

    return () => {
      cancelled = true;
    };
  }, [docTitleCache, isOpen, routeDocumentId]);

  const loadConversations = useCallback(async () => {
    if (!isAuthenticated) return [];

    const data = await aiChatApi.getConversations();
    const nextConversations = Array.isArray(data) ? data : [];
    setConversations(nextConversations);
    return nextConversations;
  }, [isAuthenticated]);

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

  const openWidget = () => {
    setIsOpen(true);
    setUnreadCount(0);
  };

  const closeWidget = () => {
    setIsOpen(false);
    setIsHistoryPanelOpen(false);
  };

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

  const startNewConversation = () => {
    setConversationId(null);
    setMessages([createGreeting()]);
    setIsHistoryPanelOpen(false);
  };

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

  const sendMessage = async (text) => {
    const cleanText = text.trim();
    if (!cleanText || isTyping || isHistoryLoading) return;

    const userMessage = {
      id: `user-${Date.now()}`,
      role: "user",
      text: cleanText,
    };

    const documentIdForRequest =
      routeDocumentId && docContextEnabled ? routeDocumentId : null;

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
        text: response.answer || "Tôi chưa có câu trả lời phù hợp.",
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
        text: "Xin lỗi, có lỗi xảy ra...",
        isError: true,
      };

      setMessages((prev) => [...prev, errorMessage]);
      setUnreadCount((count) => (isOpenRef.current ? 0 : count + 1));
    } finally {
      setIsTyping(false);
    }
  };

  const currentDocTitle = routeDocumentId
    ? docTitleCache[routeDocumentId] || "tài liệu này"
    : null;

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

export function useAiWidget() {
  const context = useContext(AiWidgetContext);
  if (!context) {
    throw new Error("useAiWidget must be used inside <AiWidgetProvider>");
  }
  return context;
}
