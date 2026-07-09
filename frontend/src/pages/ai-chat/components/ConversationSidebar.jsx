function formatConversationTime(value) {
  if (!value) return "";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";

  const now = Date.now();
  const diffMs = now - date.getTime();
  const diffMinutes = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMinutes / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffMinutes < 1) return "Vừa xong";
  if (diffMinutes < 60) return `${diffMinutes} phút trước`;
  if (diffHours < 24) return `${diffHours} giờ trước`;
  if (diffDays < 7) return `${diffDays} ngày trước`;

  return date.toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
  });
}

export default function ConversationSidebar({
  isOpen,
  isAuthenticated,
  conversations,
  activeConversationId,
  loading,
  onNewChat,
  onSelectConversation,
}) {
  return (
    <aside className={`chat-sidebar${isOpen ? " chat-sidebar--open" : ""}`}>
      <div className="chat-sidebar__top">
        <button
          type="button"
          className="chat-sidebar__new"
          onClick={onNewChat}
          disabled={loading}
        >
          + Cuộc trò chuyện mới
        </button>
      </div>

      {!isAuthenticated ? (
        <div className="chat-sidebar__hint">
          Đăng nhập để lưu lịch sử trò chuyện với AI HiveMind.
        </div>
      ) : loading ? (
        <div className="chat-sidebar__hint">Đang tải lịch sử...</div>
      ) : conversations.length === 0 ? (
        <div className="chat-sidebar__hint">
          Chưa có cuộc trò chuyện nào. Hãy bắt đầu một câu hỏi mới.
        </div>
      ) : (
        <div className="chat-sidebar__list" aria-label="Lịch sử trò chuyện">
          {conversations.map((conversation) => {
            const hasDocument = Boolean(conversation.documentId);

            return (
              <button
                key={conversation.id}
                type="button"
                className={`chat-sidebar__item${
                  conversation.id === activeConversationId
                    ? " chat-sidebar__item--active"
                    : ""
                }`}
                onClick={() => onSelectConversation(conversation.id)}
              >
                <span className="chat-sidebar__item-title">
                  {hasDocument && (
                    <span
                      className="chat-sidebar__doc-marker"
                      aria-label="Cuộc trò chuyện theo tài liệu"
                    >
                      TL
                    </span>
                  )}
                  <span className="chat-sidebar__item-title-text">
                    {conversation.title || "Cuộc trò chuyện mới"}
                  </span>
                </span>
                {hasDocument && (
                  <span className="chat-sidebar__doc-title">
                    {conversation.documentTitle ||
                      `Tài liệu #${conversation.documentId}`}
                  </span>
                )}
                <span className="chat-sidebar__item-time">
                  {formatConversationTime(
                    conversation.updatedAt || conversation.createdAt
                  )}
                </span>
              </button>
            );
          })}
        </div>
      )}
    </aside>
  );
}
