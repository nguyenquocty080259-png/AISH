import { useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import * as aiChatApi from "../../../api/aiChatApi";
import * as documentApi from "../../../api/documentApi";
import { useToast } from "../../../hooks/useToast";

export function useAiChatPage() {
  const [searchParams] = useSearchParams();
  const documentId = searchParams.get("documentId") || null;
  const { showError } = useToast();

  const [contextDoc, setContextDoc] = useState(null);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);
  const bottomRef = useRef(null);

  // Lấy tên tài liệu để hiển thị banner ngữ cảnh (chỉ phục vụ hiển thị,
  // không ảnh hưởng tới logic gọi AI).
  useEffect(() => {
    if (!documentId) {
      setContextDoc(null);
      return;
    }
    documentApi
      .getOne(documentId)
      .then((doc) => setContextDoc(doc))
      .catch(() => setContextDoc(null));
  }, [documentId]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, sending]);

  const handleSend = async (e) => {
    e.preventDefault();
    const text = input.trim();
    if (!text || sending) return;

    const userMessage = { id: `u-${Date.now()}`, role: "user", text };
    setMessages((prev) => [...prev, userMessage]);
    setInput("");
    setSending(true);

    try {
      // GHI CHÚ: backend hiện không trả lại conversationId trong AiChatResponse,
      // nên mỗi lượt chat được gửi độc lập (không có bộ nhớ hội thoại phía server).
      const res = await aiChatApi.chat({
        message: text,
        documentId: documentId ? Number(documentId) : null,
        conversationId: null,
      });
      // Backend trả { answer, mode, citations[], relatedDocs[] } (AiChatResponse) -
      // KHÔNG có field "message". citations/relatedDocs mặc định [] nếu backend không gửi.
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
    } catch (err) {
      showError(err.message || "AI hiện không phản hồi được, vui lòng thử lại.");
    } finally {
      setSending(false);
    }
  };

  return {
    documentId,
    contextDoc,
    messages,
    input,
    setInput,
    sending,
    handleSend,
    bottomRef,
  };
}
