import { useTranslation } from "react-i18next";
import { useAiWidget } from "../../context/AiWidgetContext";
import "./ai-widget.css";

export default function ChatHeader() {
  const { t } = useTranslation();
  const { closeWidget, isAuthenticated, toggleHistoryPanel } = useAiWidget();

  return (
    <header className="ai-widget__header">
      <div className="ai-widget__avatar" aria-hidden="true">AI</div>
      <div className="ai-widget__meta">
        <p className="ai-widget__name">AI HiveMind</p>
        <p className="ai-widget__status">{t("aiWidget.status")}</p>
      </div>
      <div className="ai-widget__actions">
        {isAuthenticated && (
          <button
            type="button"
            className="ai-widget__action has-custom-focus"
            onClick={toggleHistoryPanel}
            title={t("aiWidget.historyTitle")}
            aria-label={t("aiWidget.historyTitle")}
          >
            <span aria-hidden="true">≡</span>
          </button>
        )}
        <button
          type="button"
          className="ai-widget__action has-custom-focus"
          onClick={closeWidget}
          title={t("aiWidget.minimize")}
          aria-label={t("aiWidget.minimize")}
        >
          <span aria-hidden="true">-</span>
        </button>
        <button
          type="button"
          className="ai-widget__action has-custom-focus"
          onClick={closeWidget}
          title={t("aiWidget.close")}
          aria-label={t("aiWidget.close")}
        >
          <span aria-hidden="true">×</span>
        </button>
      </div>
    </header>
  );
}
