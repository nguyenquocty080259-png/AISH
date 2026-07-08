import { Link } from "react-router-dom";
import { useAiChatPage } from "./hooks/useAiChatPage";
import ChatMessage from "./components/ChatMessage";
import ChatInput from "./components/ChatInput";
import ConversationSidebar from "./components/ConversationSidebar";
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
  } = useAiChatPage();

  const renderEmptyState = () => {
    if (loadingMessages) {
      return "Đang tải lịch sử trò chuyện...";
    }

    if (isUnsupportedFormat) {
      return "AI HiveMind không đọc được nội dung của tài liệu này, nên không thể trả lời câu hỏi về tài liệu.";
    }

    if (documentId) {
      return "Đặt câu hỏi liên quan tới tài liệu này để AI HiveMind hỗ trợ bạn.";
    }

    if (isAuthenticated && conversations.length === 0) {
      return "Bắt đầu cuộc trò chuyện đầu tiên với AI HiveMind.";
    }

    return "Hỏi AI HiveMind bất cứ điều gì về HiveMind hoặc kiến thức học tập.";
  };

  return (
    <div className="chat-page">
      <ConversationSidebar
        isOpen={sidebarOpen}
        isAuthenticated={isAuthenticated}
        conversations={conversations}
        activeConversationId={activeConversationId}
        loading={loadingConversations}
        onNewChat={handleNewChat}
        onSelectConversation={handleSelectConversation}
      />

      {sidebarOpen && (
        <button
          type="button"
          className="chat-sidebar__backdrop"
          aria-label="Đóng lịch sử trò chuyện"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      <main className="chat-main">
        <div className="chat-page__header">
          <button
            type="button"
            className="chat-page__menu-btn"
            aria-label="Mở lịch sử trò chuyện"
            onClick={() => setSidebarOpen(true)}
          >
            ☰
          </button>
          <div>
            <h1 className="chat-page__title">AI HiveMind</h1>
            {documentId && (
              <Link
                to={buildRoute(ROUTES.DOCUMENT_DETAIL, { id: documentId })}
                className="chat-page__context"
              >
                Đang hỏi về: {contextDoc?.title || `tài liệu #${documentId}`}
              </Link>
            )}
          </div>
        </div>

        <div className="chat-window">
          {(messages.length === 0 || loadingMessages) && (
            <p className="chat-window__empty">{renderEmptyState()}</p>
          )}
          {!loadingMessages &&
            messages.map((m) => (
              <ChatMessage
                key={m.id}
                role={m.role}
                text={m.text}
                mode={m.mode}
                citations={m.citations}
                relatedDocs={m.relatedDocs}
              />
            ))}
          {sending && (
            <ChatMessage role="ai" text="AI HiveMind đang soạn câu trả lời..." />
          )}
          <div ref={bottomRef} />
        </div>

        {isUnsupportedFormat ? (
          <p className="chat-unsupported-notice">
            AI HiveMind không đọc được nội dung của tài liệu này (định dạng file
            không được hỗ trợ), nên không thể trả lời câu hỏi về tài liệu này.
            Bạn vẫn có thể xem hoặc tải tài liệu như bình thường.
          </p>
        ) : (
          <ChatInput
            value={input}
            onChange={setInput}
            onSubmit={handleSend}
            sending={sending}
            disabled={isInputDisabled}
          />
        )}
      </main>
    </div>
  );
}
