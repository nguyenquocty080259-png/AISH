import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import * as aiChatApi from "../../../api/aiChatApi";
import * as documentApi from "../../../api/documentApi";
import { useAuth } from "../../../hooks/useAuth";
import { useToast } from "../../../hooks/useToast";

function normalizeHistoryMessage(message) {
  return {
    id: `history-${message.id}`,
    role: message.role === "USER" ? "user" : "ai",
    text: message.content,
    createdAt: message.createdAt,
  };
}

export function useAiChatPage() {
  const [searchParams] = useSearchParams();
  const documentId = searchParams.get("documentId") || null;
  const { isAuthenticated, loading: authLoading } = useAuth();
  const { showError } = useToast();

  const [contextDoc, setContextDoc] = useState(null);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);
  const [conversations, setConversations] = useState([]);
  const [activeConversationId, setActiveConversationId] = useState(null);
  const [useDocumentContext, setUseDocumentContext] = useState(Boolean(documentId));
  const [loadingConversations, setLoadingConversations] = useState(false);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const bottomRef = useRef(null);

  useEffect(() => {
    setUseDocumentContext(Boolean(documentId));

    if (!documentId) {
      setContextDoc(null);
      return;
    }

    documentApi
      .getOne(documentId)
      .then((doc) => setContextDoc(doc))
      .catch(() => setContextDoc(null));
  }, [documentId]);

  const isUnsupportedFormat = Boolean(
    documentId && useDocumentContext && contextDoc?.ingestStatus === "UNSUPPORTED_FORMAT"
  );

  const activeConversation = conversations.find(
    (conversation) => conversation.id === activeConversationId
  );

  const activeDocumentId =
    activeConversation?.documentId ||
    (!activeConversationId && useDocumentContext && documentId
      ? Number(documentId)
      : null);

  const activeDocumentTitle =
    activeConversation?.documentTitle ||
    (!activeConversationId && useDocumentContext && documentId
      ? contextDoc?.title || `tài liệu #${documentId}`
      : null);

  const loadMessages = useCallback(
    async (conversationId) => {
      if (!conversationId) return;

      setLoadingMessages(true);
      try {
        const data = await aiChatApi.getMessages(conversationId);
        setMessages((data || []).map(normalizeHistoryMessage));
        setActiveConversationId(conversationId);
      } catch (err) {
        showError(
          err.message || "Không tải được lịch sử trò chuyện, vui lòng thử lại."
        );
      } finally {
        setLoadingMessages(false);
      }
    },
    []
  );

  const loadConversations = useCallback(
    async ({ autoSelectLatest = false, keepActiveId = null } = {}) => {
      if (!isAuthenticated || authLoading) return [];

      setLoadingConversations(true);
      try {
        const data = await aiChatApi.getConversations();
        const nextConversations = data || [];
        setConversations(nextConversations);

        if (keepActiveId) {
          setActiveConversationId(keepActiveId);
        } else if (autoSelectLatest && nextConversations.length > 0) {
          await loadMessages(nextConversations[0].id);
        } else if (autoSelectLatest) {
          setActiveConversationId(null);
          setMessages([]);
        }

        return nextConversations;
      } catch (err) {
        showError(
          err.message || "Không tải được danh sách trò chuyện, vui lòng thử lại."
        );
        return [];
      } finally {
        setLoadingConversations(false);
      }
    },
    [authLoading, isAuthenticated, loadMessages]
  );

  useEffect(() => {
    if (authLoading) return;

    if (!isAuthenticated) {
      setConversations([]);
      setActiveConversationId(null);
      return;
    }

    loadConversations({ autoSelectLatest: !documentId });
  }, [authLoading, documentId, isAuthenticated, loadConversations]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, sending, loadingMessages]);

  const closeMobileSidebar = () => setSidebarOpen(false);

  const handleSelectConversation = async (conversationId) => {
    if (!isAuthenticated || conversationId === activeConversationId) {
      closeMobileSidebar();
      return;
    }

    setUseDocumentContext(false);
    await loadMessages(conversationId);
    closeMobileSidebar();
  };

  const handleNewChat = () => {
    setActiveConversationId(null);
    setUseDocumentContext(false);
    setMessages([]);
    setInput("");
    closeMobileSidebar();
  };

  const isInputDisabled =
    sending || loadingMessages || loadingConversations || authLoading;

  const handleSend = async (e) => {
    e.preventDefault();
    const text = input.trim();
    if (!text || isInputDisabled) return;

    const userMessage = { id: `u-${Date.now()}`, role: "user", text };
    const conversationIdForRequest = isAuthenticated
      ? activeConversationId
      : null;
    const documentIdForRequest =
      !activeConversationId && useDocumentContext && documentId
        ? Number(documentId)
        : null;

    setMessages((prev) => [...prev, userMessage]);
    setInput("");
    setSending(true);

    try {
      const res = await aiChatApi.chat({
        message: text,
        documentId: documentIdForRequest,
        conversationId: conversationIdForRequest,
      });

      const nextConversationId = res.conversationId || conversationIdForRequest;
      if (nextConversationId) {
        setActiveConversationId(nextConversationId);
      }

      setMessages((prev) => [
        ...prev,
        {
          id: `a-${Date.now()}`,
          role: "ai",
          text: res.answer,
          mode: res.mode,
          citations: res.citations || [],
          relatedDocs: res.relatedDocs || [],
        },
      ]);

      if (isAuthenticated && nextConversationId) {
        await loadConversations({ keepActiveId: nextConversationId });
      }
    } catch (err) {
      showError(
        err.message ||
          "AI HiveMind hiện không phản hồi được, vui lòng thử lại."
      );
    } finally {
      setSending(false);
    }
  };

  return {
    documentId,
    contextDoc,
    activeDocumentId,
    activeDocumentTitle,
    messages,
    input,
    setInput,
    sending,
    handleSend,
    bottomRef,
    isUnsupportedFormat,
    isAuthenticated,
    conversations,
    activeConversationId,
    loadingConversations,
    loadingMessages,
    sidebarOpen,
    setSidebarOpen,
    handleSelectConversation,
    handleNewChat,
    isInputDisabled,
  };
}
