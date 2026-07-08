import { createContext, useContext, useEffect, useMemo, useRef, useState } from "react";
import * as aiChatApi from "../api/aiChatApi";
import { useAuth } from "../hooks/useAuth";

const AiWidgetContext = createContext(null);

const createGreeting = () => ({
  id: "ai-greeting",
  role: "ai",
  text: "Xin chào  Tôi có thể giúp gì cho bạn?",
});

export function AiWidgetProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([createGreeting()]);
  const [isTyping, setIsTyping] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [conversationId, setConversationId] = useState(null);
  const [historyLoaded, setHistoryLoaded] = useState(false);
  const [isHistoryLoading, setIsHistoryLoading] = useState(false);
  const isOpenRef = useRef(isOpen);

  useEffect(() => {
    isOpenRef.current = isOpen;
  }, [isOpen]);

  const openWidget = () => {
    setIsOpen(true);
    setUnreadCount(0);
  };

  const closeWidget = () => {
    setIsOpen(false);
  };

  useEffect(() => {
    if (!isAuthenticated) {
      setHistoryLoaded(false);
      setIsHistoryLoading(false);
      setConversationId(null);
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
        const conversations = await aiChatApi.getConversations();
        const latest = Array.isArray(conversations) ? conversations[0] : null;
        if (!latest?.id || cancelled) {
          setHistoryLoaded(true);
          return;
        }

        const history = await aiChatApi.getMessages(latest.id);
        if (cancelled) return;

        const mappedMessages = Array.isArray(history)
          ? history.map((message) => ({
              id: `history-${message.id}`,
              role: message.role === "USER" ? "user" : "ai",
              text: message.content,
            }))
          : [];

        setConversationId(latest.id);
        setMessages(mappedMessages.length > 0 ? mappedMessages : [createGreeting()]);
      } catch (error) {
        console.error("Failed to load AI HiveMind history", error);
        setMessages((prev) => prev);
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
  }, [conversationId, historyLoaded, isAuthenticated, isOpen, messages]);

  const sendMessage = async (text) => {
    const cleanText = text.trim();
    if (!cleanText || isTyping || isHistoryLoading) return;

    const userMessage = {
      id: `user-${Date.now()}`,
      role: "user",
      text: cleanText,
    };

    setMessages((prev) => [...prev, userMessage]);
    setIsTyping(true);

    try {
      const response = await aiChatApi.chat({
        message: cleanText,
        documentId: null,
        conversationId,
      });

      if (response.conversationId) {
        setConversationId(response.conversationId);
      }

      const aiMessage = {
        id: `ai-${Date.now()}`,
        role: "ai",
        text: response.answer || "Tôi chưa có câu trả lời phù hợp.",
        mode: response.mode,
      };

      setMessages((prev) => [...prev, aiMessage]);
      setUnreadCount((count) => (isOpenRef.current ? 0 : count + 1));
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

  const value = useMemo(
    () => ({
      isOpen,
      messages,
      isTyping,
      isHistoryLoading,
      unreadCount,
      conversationId,
      openWidget,
      closeWidget,
      sendMessage,
    }),
    [conversationId, isHistoryLoading, isOpen, messages, isTyping, unreadCount]
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
