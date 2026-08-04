import { useTranslation } from "react-i18next";
import { useAiWidget } from "../../context/AiWidgetContext";
import "./ai-widget.css";

// Nút tròn nổi ở góc màn hình để mở widget chat, kèm số tin nhắn chưa đọc.
export default function FloatingButton() {
  const { t } = useTranslation();
  const { openWidget, unreadCount } = useAiWidget();

  return (
    <button
      type="button"
      aria-label={t("aiWidget.open")}
      title="AI HiveMind"
      className="ai-widget__fab has-custom-focus"
      onClick={openWidget}
    >
      <span aria-hidden="true">AI</span>
      {unreadCount > 0 && (
        <span className="ai-widget__fab-badge">{unreadCount > 9 ? "9+" : unreadCount}</span>
      )}
    </button>
  );
}
