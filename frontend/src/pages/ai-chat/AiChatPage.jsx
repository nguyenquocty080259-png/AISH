import { Link } from "react-router-dom";
import { useAiChatPage } from "./hooks/useAiChatPage";
import ChatMessage from "./components/ChatMessage";
import ChatInput from "./components/ChatInput";
import { ROUTES, buildRoute } from "../../constants/routes";
import "./ai-chat.css";

export default function AiChatPage() {
  const {
    documentId,
    contextDoc,
    messages,
    input,
    setInput,
    sending,
    handleSend,
    bottomRef,
  } = useAiChatPage();

  return (
    <div className="chat-page">
      <div className="chat-page__header">
        <h1 className="chat-page__title">AI Chat</h1>
        {documentId && (
          <Link
            to={buildRoute(ROUTES.DOCUMENT_DETAIL, { id: documentId })}
            className="chat-page__context"
          >
            Đang hỏi về: {contextDoc?.title || `tài liệu #${documentId}`}
          </Link>
        )}
      </div>

      <div className="chat-window">
        {messages.length === 0 && (
          <p className="chat-window__empty">
            {documentId
              ? "Đặt câu hỏi liên quan tới tài liệu này để AI hỗ trợ bạn."
              : "Hỏi AI bất cứ điều gì về AISH hoặc kiến thức học tập."}
          </p>
        )}
        {messages.map((m) => (
          <ChatMessage key={m.id} role={m.role} text={m.text} mode={m.mode} />
        ))}
        {sending && (
          <ChatMessage role="ai" text="AI đang soạn câu trả lời..." />
        )}
        <div ref={bottomRef} />
      </div>

      <ChatInput
        value={input}
        onChange={setInput}
        onSubmit={handleSend}
        sending={sending}
      />
    </div>
  );
}
