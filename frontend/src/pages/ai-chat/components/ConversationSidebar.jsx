import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import Button from "../../../components/ui/Button";
import Modal from "../../../components/ui/Modal";

// Định dạng thời gian cập nhật cuối của cuộc trò chuyện thành dạng tương đối ("5 phút trước"...).
function formatConversationTime(value, t, locale) {
  if (!value) return "";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";

  const now = Date.now();
  const diffMs = now - date.getTime();
  const diffMinutes = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMinutes / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffMinutes < 1) return t("common.time.justNow");
  if (diffMinutes < 60) return t("common.time.minutesAgo", { count: diffMinutes });
  if (diffHours < 24) return t("common.time.hoursAgo", { count: diffHours });
  if (diffDays < 7) return t("common.time.daysAgo", { count: diffDays });

  return date.toLocaleDateString(locale, {
    day: "2-digit",
    month: "2-digit",
  });
}

// Sidebar LỊCH SỬ CHAT: danh sách cuộc trò chuyện (đánh dấu cuộc gắn với tài liệu), nút tạo mới,
// và modal đổi tên/xoá cho từng cuộc.
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
  const { t, i18n } = useTranslation();
  const locale = i18n.resolvedLanguage === "en" ? "en-US" : "vi-VN";
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
          {t("aiChat.newConversation")}
        </button>
      </div>

      {!isAuthenticated ? (
        <div className="chat-sidebar__hint">
          {t("aiChat.loginToSave")}
        </div>
      ) : loading ? (
        <div className="chat-sidebar__hint">{t("aiChat.loadingHistoryShort")}</div>
      ) : conversations.length === 0 ? (
        <div className="chat-sidebar__hint">
          {t("aiChat.noConversations")}
        </div>
      ) : (
        <div className="chat-sidebar__list" aria-label={t("aiChat.historyAria")}>
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
                      aria-label={t("aiChat.docConvAria")}
                    >
                      {t("aiChat.docMarker")}
                    </span>
                  )}
                  <span className="chat-sidebar__item-title-text">
                    {conversation.title || t("aiChat.untitled")}
                  </span>
                </span>
                {hasDocument && (
                  <span className="chat-sidebar__doc-title">
                    {conversation.documentTitle ||
                      t("aiChat.docFallbackHash", { id: conversation.documentId })}
                  </span>
                )}
                <span className="chat-sidebar__item-time">
                  {formatConversationTime(
                    conversation.updatedAt || conversation.createdAt,
                    t,
                    locale
                  )}
                </span>
                <span style={{ display: "flex", gap: 4, marginTop: 4 }}>
                  <button type="button" aria-label={t("aiChat.renameAria")}
                    style={{ padding: 2, border: 0, background: "transparent", cursor: "pointer" }}
                    onClick={(event) => { event.stopPropagation(); setRenameTarget(conversation); }}>
                    ✏️
                  </button>
                  <button type="button" aria-label={t("aiChat.deleteAria")}
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

    <Modal open={!!renameTarget} onClose={() => !submitting && setRenameTarget(null)} title={t("aiChat.renameTitle")}>
      <form onSubmit={submitRename}>
        <label style={{ display: "grid", gap: 8 }}>{t("aiChat.renameLabel")}
          <input autoFocus type="text" value={renameTitle} onChange={(event) => setRenameTitle(event.target.value)} />
        </label>
        <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 20 }}>
          <Button variant="secondary" onClick={() => setRenameTarget(null)} disabled={submitting}>{t("common.actions.cancel")}</Button>
          <Button type="submit" disabled={submitting || !renameTitle.trim()}>{submitting ? t("aiChat.saving") : t("aiChat.save")}</Button>
        </div>
      </form>
    </Modal>

    <Modal open={!!deleteTarget} onClose={() => !submitting && setDeleteTarget(null)} title={t("aiChat.deleteTitle")}>
      <p>{t("aiChat.deleteConfirm", { title: deleteTarget?.title })}</p>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 20 }}>
        <Button variant="secondary" onClick={() => setDeleteTarget(null)} disabled={submitting}>{t("common.actions.cancel")}</Button>
        <Button variant="danger" onClick={confirmDelete} disabled={submitting}>{submitting ? t("aiChat.deleting") : t("aiChat.delete")}</Button>
      </div>
    </Modal>
    </>
  );
}
