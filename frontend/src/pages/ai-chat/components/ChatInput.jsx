import { useTranslation } from "react-i18next";

// Ô nhập tin nhắn + nút gửi ở cuối khung chat.
export default function ChatInput({
  value,
  onChange,
  onSubmit,
  sending,
  disabled = false,
}) {
  const { t } = useTranslation();
  const isDisabled = sending || disabled;

  return (
    <form className="chat-input" onSubmit={onSubmit}>
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={t("aiChat.inputPlaceholder")}
        disabled={isDisabled}
      />
      <button type="submit" disabled={isDisabled || !value.trim()}>
        {sending ? t("aiChat.sending") : t("aiChat.send")}
      </button>
    </form>
  );
}
