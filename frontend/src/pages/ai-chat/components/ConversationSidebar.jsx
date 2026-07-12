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
  onRenameConversation,
  onDeleteConversation,
}) {
  const [renameTarget, setRenameTarget] = useState(null);
  const [renameTitle, setRenameTitle] = useState("");
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    setRenameTitle(renameTarget?.title ?? "");
  }, [renameTarget]);

  const submitRename = async (event) => {
    event.preventDefault();
    if (!renameTarget || !renameTitle.trim()) return;
    setSubmitting(true);
    try {
      const succeeded = await onRenameConversation(renameTarget.id, renameTitle.trim());
      if (succeeded) setRenameTarget(null);
    } finally {
      setSubmitting(false);
    }
  };

  const confirmDelete = async () => {
    if (!deleteTarget) return;
    setSubmitting(true);
    try {
      const succeeded = await onDeleteConversation(deleteTarget.id);
      if (succeeded) setDeleteTarget(null);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
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
              <div
                key={conversation.id}
                role="button"
                tabIndex={0}
                className={`chat-sidebar__item${
                  conversation.id === activeConversationId
                    ? " chat-sidebar__item--active"
                    : ""
                }`}
                onClick={() => onSelectConversation(conversation.id)}
                onKeyDown={(event) => {
                  if (event.key === "Enter" || event.key === " ") onSelectConversation(conversation.id);
                }}
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
                <span style={{ display: "flex", gap: 4, marginTop: 4 }}>
                  <button type="button" aria-label="Đổi tên cuộc trò chuyện"
                    style={{ padding: 2, border: 0, background: "transparent", cursor: "pointer" }}
                    onClick={(event) => { event.stopPropagation(); setRenameTarget(conversation); }}>
                    ✏️
                  </button>
                  <button type="button" aria-label="Xóa cuộc trò chuyện"
                    style={{ padding: 2, border: 0, background: "transparent", cursor: "pointer" }}
                    onClick={(event) => { event.stopPropagation(); setDeleteTarget(conversation); }}>
                    🗑️
                  </button>
                </span>
              </div>
            );
          })}
        </div>
      )}
    </aside>

    <Modal open={!!renameTarget} onClose={() => !submitting && setRenameTarget(null)} title="Đổi tên cuộc trò chuyện">
      <form onSubmit={submitRename}>
        <label style={{ display: "grid", gap: 8 }}>Tên cuộc trò chuyện
          <input autoFocus type="text" value={renameTitle} onChange={(event) => setRenameTitle(event.target.value)} />
        </label>
        <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 20 }}>
          <Button variant="secondary" onClick={() => setRenameTarget(null)} disabled={submitting}>Hủy</Button>
          <Button type="submit" disabled={submitting || !renameTitle.trim()}>{submitting ? "Đang lưu..." : "Lưu"}</Button>
        </div>
      </form>
    </Modal>

    <Modal open={!!deleteTarget} onClose={() => !submitting && setDeleteTarget(null)} title="Xóa cuộc trò chuyện">
      <p>Bạn có chắc muốn xóa “{deleteTarget?.title}”? Toàn bộ tin nhắn trong cuộc trò chuyện sẽ bị xóa.</p>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 20 }}>
        <Button variant="secondary" onClick={() => setDeleteTarget(null)} disabled={submitting}>Hủy</Button>
        <Button variant="danger" onClick={confirmDelete} disabled={submitting}>{submitting ? "Đang xóa..." : "Xóa"}</Button>
      </div>
    </Modal>
    </>
  );
}
import { useEffect, useState } from "react";
import Button from "../../../components/ui/Button";
import Modal from "../../../components/ui/Modal";
