import { useAiWidget } from "../../context/AiWidgetContext";

function formatConversationTime(value) {
  if (!value) return "";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";

  const diffMinutes = Math.floor((Date.now() - date.getTime()) / 60000);
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

const styles = {
  panel: {
    position: "absolute",
    inset: 0,
    zIndex: 2,
    display: "flex",
    flexDirection: "column",
    background: "#fffaf0",
    borderTop: "1px solid rgba(15, 23, 42, 0.08)",
  },
  top: {
    padding: 10,
    borderBottom: "1px solid rgba(15, 23, 42, 0.08)",
    background: "#fff",
  },
  newButton: {
    width: "100%",
    border: "1px solid #fed7aa",
    borderRadius: 12,
    background: "#fff7ed",
    color: "#9a3412",
    padding: "9px 10px",
    fontWeight: 800,
    textAlign: "left",
    cursor: "pointer",
  },
  list: {
    flex: 1,
    overflowY: "auto",
    padding: 8,
  },
  item: {
    width: "100%",
    border: "none",
    borderRadius: 12,
    background: "transparent",
    padding: 9,
    textAlign: "left",
    cursor: "pointer",
    display: "flex",
    flexDirection: "column",
    gap: 3,
  },
  activeItem: {
    background: "rgba(249, 115, 22, 0.14)",
  },
  title: {
    display: "flex",
    alignItems: "center",
    gap: 6,
    minWidth: 0,
    color: "#1f2937",
    fontSize: 13,
    fontWeight: 800,
  },
  titleText: {
    overflow: "hidden",
    whiteSpace: "nowrap",
    textOverflow: "ellipsis",
  },
  marker: {
    flex: "0 0 auto",
    borderRadius: 999,
    background: "rgba(249, 115, 22, 0.14)",
    color: "#c2410c",
    padding: "1px 5px",
    fontSize: 10,
    fontWeight: 800,
  },
  docTitle: {
    overflow: "hidden",
    whiteSpace: "nowrap",
    textOverflow: "ellipsis",
    color: "#9a3412",
    fontSize: 12,
  },
  time: {
    color: "#78716c",
    fontSize: 12,
  },
  hint: {
    padding: 14,
    color: "#78716c",
    fontSize: 13,
    lineHeight: 1.45,
  },
};

export default function HistoryPanel() {
  const {
    conversations,
    conversationId,
    isHistoryLoading,
    startNewConversation,
    selectConversation,
  } = useAiWidget();

  return (
    <div style={styles.panel}>
      <div style={styles.top}>
        <button
          type="button"
          style={styles.newButton}
          onClick={startNewConversation}
          disabled={isHistoryLoading}
        >
          + Cuộc trò chuyện mới
        </button>
      </div>

      {conversations.length === 0 ? (
        <div style={styles.hint}>Chưa có cuộc trò chuyện nào.</div>
      ) : (
        <div style={styles.list} aria-label="Lịch sử trò chuyện AI HiveMind">
          {conversations.map((conversation) => {
            const hasDocument = Boolean(conversation.documentId);
            const isActive = conversation.id === conversationId;

            return (
              <button
                key={conversation.id}
                type="button"
                style={{
                  ...styles.item,
                  ...(isActive ? styles.activeItem : {}),
                }}
                onClick={() => selectConversation(conversation.id)}
                disabled={isHistoryLoading}
              >
                <span style={styles.title}>
                  {hasDocument && <span style={styles.marker}>TL</span>}
                  <span style={styles.titleText}>
                    {conversation.title || "Cuộc trò chuyện mới"}
                  </span>
                </span>
                {hasDocument && (
                  <span style={styles.docTitle}>
                    {conversation.documentTitle ||
                      `Tài liệu #${conversation.documentId}`}
                  </span>
                )}
                <span style={styles.time}>
                  {formatConversationTime(
                    conversation.updatedAt || conversation.createdAt
                  )}
                </span>
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
