import { createContext, useContext, useEffect, useMemo, useRef, useState } from "react";
import * as aiChatApi from "../api/aiChatApi";

const AiWidgetContext = createContext(null);

const createGreeting = () => ({
  id: "ai-greeting",
  role: "ai",
  text: "Xin chào  Tôi có thể giúp gì cho bạn?",
});

export function AiWidgetProvider({ children }) {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([createGreeting()]);
  const [isTyping, setIsTyping] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
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

  const sendMessage = async (text) => {
    const cleanText = text.trim();
    if (!cleanText || isTyping) return;

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
        conversationId: null,
      });

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
      unreadCount,
      openWidget,
      closeWidget,
      sendMessage,
    }),
    [isOpen, messages, isTyping, unreadCount]
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
