import { useTranslation } from "react-i18next";
import { useAiWidget } from "../../context/AiWidgetContext";
import "./ai-widget.css";

// Thanh bật/tắt "hỏi AI về tài liệu này" — chỉ hiện khi đang đứng ở trang chi tiết 1 tài liệu.
// Tắt nếu tài liệu chưa được nạp cho AI (ingest) xong.
export default function DocContextBar() {
  const { t } = useTranslation();
  const {
    routeDocumentId,
    currentDocTitle,
    docContextEnabled,
    currentDocReady,
    setDocContextEnabled,
  } = useAiWidget();

  if (!routeDocumentId) return null;

  const toggleDisabled = !currentDocReady;
  const on = docContextEnabled && !toggleDisabled;

  return (
    <div className="ai-widget__doccontext">
      <div className="ai-widget__doccontext-row">
        <span className="ai-widget__doccontext-label">
          {t("aiWidget.askAbout", { title: currentDocTitle })}
        </span>
        <button
          type="button"
          className="ai-widget__doccontext-toggle has-custom-focus"
          onClick={() => setDocContextEnabled((enabled) => !enabled)}
          aria-pressed={on}
          title={docContextEnabled ? t("aiWidget.toggleOn") : t("aiWidget.toggleOff")}
          disabled={toggleDisabled}
        >
          <span
            className={`ai-widget__doccontext-track ${
              on ? "ai-widget__doccontext-track--on" : ""
            }`.trim()}
            aria-hidden="true"
          >
            <span className="ai-widget__doccontext-knob" />
          </span>
          {on ? "ON" : "OFF"}
        </button>
      </div>
      {toggleDisabled && (
        <span className="ai-widget__doccontext-warning">
          {t("aiWidget.notIngestedWarning")}
        </span>
      )}
    </div>
  );
}
