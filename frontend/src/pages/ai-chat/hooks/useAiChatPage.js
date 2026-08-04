import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as aiChatApi from "../../../api/aiChatApi";
import * as documentApi from "../../../api/documentApi";
import { useAuth } from "../../../hooks/useAuth";
import { useToast } from "../../../hooks/useToast";

// Chuyển tin nhắn lấy từ lịch sử (BE) sang định dạng hiển thị trong trang chat.
function normalizeHistoryMessage(message) {
  return {
    id: `history-${message.id}`,
    role: message.role === "USER" ? "user" : "ai",
    text: message.content,
    createdAt: message.createdAt,
  };
}

/**
 * Hook TRUNG TÂM cho trang AI Chat toàn màn hình (khác widget nổi — trang này có sidebar lịch sử
 * cuộc trò chuyện, quản lý nhiều cuộc trò chuyện, và luôn đọc documentId từ query string để tự
 * bật ngữ cảnh "hỏi AI về tài liệu này" khi được điều hướng tới từ trang chi tiết tài liệu).
 */
export function useAiChatPage() {
  const { t } = useTranslation();
  const [searchParams] = useSearchParams();
  const documentId = searchParams.get("documentId") || null;
  const { isAuthenticated, loading: authLoading } = useAuth();
  const { showError, showSuccess } = useToast();

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

  // Gọi API nạp toàn bộ tin nhắn của một cuộc trò chuyện, đổi thành cuộc trò chuyện đang xem.
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
          err.message || t("aiChat.historyError")
        );
      } finally {
        setLoadingMessages(false);
      }
    },
    []
  );

  // Gọi API nạp danh sách cuộc trò chuyện cho sidebar. autoSelectLatest: tự mở cuộc trò chuyện
  // gần nhất nếu chưa vào từ 1 tài liệu cụ thể. keepActiveId: giữ nguyên cuộc đang xem sau khi
  // danh sách được nạp lại (dùng sau khi đổi tên/xoá).
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
          err.message || t("aiChat.listError")
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

  // Chọn 1 cuộc trò chuyện khác từ sidebar để xem tiếp.
  const handleSelectConversation = async (conversationId) => {
    if (!isAuthenticated || conversationId === activeConversationId) {
      closeMobileSidebar();
      return;
    }

    setUseDocumentContext(false);
    await loadMessages(conversationId);
    closeMobileSidebar();
  };

  // Bắt đầu cuộc trò chuyện mới trống (không gắn tài liệu).
  const handleNewChat = () => {
    setActiveConversationId(null);
    setUseDocumentContext(false);
    setMessages([]);
    setInput("");
    closeMobileSidebar();
  };

  // Xoá 1 cuộc trò chuyện. Xoá đúng cuộc đang xem thì chuyển sang màn "chat mới" trước khi nạp
  // lại danh sách; xoá cuộc khác thì chỉ cần bỏ khỏi danh sách và giữ nguyên cuộc đang xem.
  const handleDeleteConversation = async (id) => {
    try {
      await aiChatApi.deleteConversation(id);
      const deletingActiveConversation = id === activeConversationId;
      if (deletingActiveConversation) {
        handleNewChat();
        await loadConversations();
      } else {
        setConversations((current) => current.filter((conversation) => conversation.id !== id));
        await loadConversations({ keepActiveId: activeConversationId });
      }
      showSuccess(t("aiChat.convDeleted"));
      return true;
    } catch (error) {
      showError(error.message);
      return false;
    }
  };

  // Đổi tên 1 cuộc trò chuyện.
  const handleRenameConversation = async (id, newTitle) => {
    try {
      const updated = await aiChatApi.renameConversation(id, newTitle);
      setConversations((current) => current.map((conversation) =>
        conversation.id === id ? { ...conversation, ...updated } : conversation));
      await loadConversations({ keepActiveId: activeConversationId });
      showSuccess(t("aiChat.convRenamed"));
      return true;
    } catch (error) {
      showError(error.message);
      return false;
    }
  };

  const isInputDisabled =
    sending || loadingMessages || loadingConversations || authLoading;

  // Gửi câu hỏi cho AI. Đầu vào: sự kiện submit form. Các bước: (1) thêm tin nhắn của user vào
  // giao diện ngay; (2) gọi API /ai/chat kèm documentId nếu đang bật ngữ cảnh tài liệu VÀ đây là
  // tin nhắn ĐẦU TIÊN của một cuộc trò chuyện mới (đã có activeConversationId thì không gửi lại
  // documentId — BE tự nhớ tài liệu gắn với cuộc trò chuyện); (3) thêm câu trả lời AI; (4) nạp
  // lại danh sách cuộc trò chuyện để cập nhật tiêu đề/thời gian.
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
          t("aiChat.noResponse")
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
    handleDeleteConversation,
    handleRenameConversation,
    isInputDisabled,
  };
}
