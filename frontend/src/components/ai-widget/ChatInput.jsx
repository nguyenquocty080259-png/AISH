import { useState } from "react";
import { useTranslation } from "react-i18next";

const styles = {
  form: {
    display: "flex",
    gap: 8,
    padding: 12,
    borderTop: "1px solid rgba(15, 23, 42, 0.08)",
    background: "#fff",
  },
  input: {
    flex: 1,
    minHeight: 40,
    maxHeight: 92,
    resize: "none",
    borderRadius: 18,
    border: "1px solid #fed7aa",
    padding: "10px 12px",
    font: "inherit",
    outline: "none",
  },
  button: {
    width: 44,
    height: 40,
    borderRadius: "50%",
    border: "none",
    background: "#f97316",
    color: "#fff",
    cursor: "pointer",
    fontWeight: 800,
    flexShrink: 0,
  },
};

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
    <form style={styles.form} onSubmit={handleSubmit}>
      <textarea
        style={styles.input}
        rows={1}
        value={value}
        onChange={(event) => setValue(event.target.value)}
        onKeyDown={handleKeyDown}
        placeholder={t("aiWidget.inputPlaceholder")}
        disabled={disabled}
      />
      <button type="submit" style={styles.button} disabled={disabled || !value.trim()}>
        ➤
      </button>
    </form>
  );
}
