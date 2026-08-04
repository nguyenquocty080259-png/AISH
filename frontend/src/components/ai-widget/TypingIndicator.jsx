import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import "./ai-widget.css";

// Hiệu ứng "AI đang gõ..." với 3 chấm nhấp nháy.
export default function TypingIndicator() {
  const { t } = useTranslation();
  const [dots, setDots] = useState(".");

  useEffect(() => {
    const id = window.setInterval(() => {
      setDots((current) => (current.length >= 3 ? "." : `${current}.`));
    }, 350);
    return () => window.clearInterval(id);
  }, []);

  return (
    <div className="ai-widget__row" role="status">
      <div className="ai-widget__bubble ai-widget__bubble--typing">
        {t("aiWidget.typing")}
        <span className="ai-widget__typing-dots" aria-hidden="true">{dots}</span>
      </div>
    </div>
  );
}
