import { useTranslation } from "react-i18next";
import { useAiWidget } from "../../context/AiWidgetContext";
import "./ai-widget.css";

// Định dạng thời gian cập nhật cuối của cuộc trò chuyện thành dạng tương đối ("5 phút trước"...).
function formatConversationTime(value, t, locale) {
  if (!value) return "";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";

  const diffMinutes = Math.floor((Date.now() - date.getTime()) / 60000);
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

// Bảng lịch sử cuộc trò chuyện hiện đè lên trong widget (khi bấm nút ≡ ở ChatHeader).
export default function HistoryPanel() {
  const { t, i18n } = useTranslation();
  const locale = i18n.resolvedLanguage === "en" ? "en-US" : "vi-VN";
  const {
    conversations,
    conversationId,
    isHistoryLoading,
    startNewConversation,
    selectConversation,
  } = useAiWidget();

  return (
    <div className="ai-widget__history">
      <div className="ai-widget__history-top">
        <button
          type="button"
          className="ai-widget__history-new has-custom-focus"
          onClick={startNewConversation}
          disabled={isHistoryLoading}
        >
          {t("aiWidget.newConversation")}
        </button>
      </div>

      {conversations.length === 0 ? (
        <p className="ai-widget__history-hint">{t("aiWidget.noConversations")}</p>
      ) : (
        <div className="ai-widget__history-list" aria-label={t("aiWidget.historyAria")}>
          {conversations.map((conversation) => {
            const hasDocument = Boolean(conversation.documentId);
            const isActive = conversation.id === conversationId;

            return (
              <button
                key={conversation.id}
                type="button"
                className={`ai-widget__history-item has-custom-focus ${
                  isActive ? "ai-widget__history-item--active" : ""
                }`.trim()}
                onClick={() => selectConversation(conversation.id)}
                disabled={isHistoryLoading}
              >
                <span className="ai-widget__history-title">
                  {hasDocument && (
                    <span className="ai-widget__history-marker">{t("aiWidget.docMarker")}</span>
                  )}
                  <span className="ai-widget__history-title-text">
                    {conversation.title || t("aiWidget.untitled")}
                  </span>
                </span>
                {hasDocument && (
                  <span className="ai-widget__history-doc">
                    {conversation.documentTitle ||
                      t("aiWidget.docFallback", { id: conversation.documentId })}
                  </span>
                )}
                <span className="ai-widget__history-time">
                  {formatConversationTime(
                    conversation.updatedAt || conversation.createdAt,
                    t,
                    locale
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
