import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAiChatPage } from "./hooks/useAiChatPage";
import ChatMessage from "./components/ChatMessage";
import ChatInput from "./components/ChatInput";
import ConversationSidebar from "./components/ConversationSidebar";
import GuestAiPromo from "./components/GuestAiPromo";
import { ROUTES, buildRoute } from "../../constants/routes";
import "./ai-chat.css";

// Trang AI CHAT toàn màn hình: sidebar lịch sử cuộc trò chuyện + khung chat chính. Khách (chưa
// đăng nhập) thấy trang quảng bá (GuestAiPromo) thay vì khung chat thật.
export default function AiChatPage() {
  const { t } = useTranslation();
  const {
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
  } = useAiChatPage();

  if (!isAuthenticated) return <GuestAiPromo />;

  const renderEmptyState = () => {
    if (loadingMessages) {
      return t("aiChat.loadingHistory");
    }

    if (isUnsupportedFormat) {
      return t("aiChat.unsupportedEmpty");
    }

    if (activeDocumentId) {
      return t("aiChat.askAboutDoc");
    }

    if (isAuthenticated && conversations.length === 0) {
      return t("aiChat.firstConversation");
    }

    return t("aiChat.askAnything");
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
        onDeleteConversation={handleDeleteConversation}
        onRenameConversation={handleRenameConversation}
      />

      {sidebarOpen && (
        <button
          type="button"
          className="chat-sidebar__backdrop"
          aria-label={t("aiChat.closeSidebar")}
          onClick={() => setSidebarOpen(false)}
        />
      )}

      <main className="chat-main">
        <div className="chat-page__header">
          <button
            type="button"
            className="chat-page__menu-btn"
            aria-label={t("aiChat.openSidebar")}
            onClick={() => setSidebarOpen(true)}
          >
            ☰
          </button>
          <div>
            <h1 className="chat-page__title">AI HiveMind</h1>
            {activeDocumentId && (
              <Link
                to={buildRoute(ROUTES.DOCUMENT_DETAIL, {
                  id: activeDocumentId,
                })}
                className="chat-page__context"
              >
                {t("aiChat.askingAbout", {
                  title:
                    activeDocumentTitle ||
                    contextDoc?.title ||
                    t("aiChat.docFallback", { id: activeDocumentId }),
                })}
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
            <ChatMessage role="ai" text={t("aiChat.composing")} />
          )}
          <div ref={bottomRef} />
        </div>

        {isUnsupportedFormat ? (
          <p className="chat-unsupported-notice">
            {t("aiChat.unsupportedNotice")}
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
