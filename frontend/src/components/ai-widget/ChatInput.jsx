import { useState } from "react";
import { useTranslation } from "react-i18next";
import "./ai-widget.css";

// Ô nhập tin nhắn của widget: Enter để gửi (Shift+Enter xuống dòng).
export default function ChatInput({ onSend, disabled }) {
  const { t } = useTranslation();
  const [value, setValue] = useState("");

  const submit = () => {
    const text = value.trim();
    if (!text || disabled) return;
    onSend(text);
    setValue("");
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    submit();
  };

  const handleKeyDown = (event) => {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      submit();
    }
  };

  return (
    <form className="ai-widget__composer" onSubmit={handleSubmit}>
      <textarea
        className="ai-widget__input has-custom-focus"
        rows={1}
        value={value}
        onChange={(event) => setValue(event.target.value)}
        onKeyDown={handleKeyDown}
        placeholder={t("aiWidget.inputPlaceholder")}
        aria-label={t("aiWidget.inputPlaceholder")}
        disabled={disabled}
      />
      <button
        type="submit"
        className="ai-widget__send has-custom-focus"
        disabled={disabled || !value.trim()}
        aria-label={t("aiWidget.send")}
      >
        <span aria-hidden="true">➤</span>
      </button>
    </form>
  );
}
