import { useEffect, useRef } from "react";
import { useTranslation } from "react-i18next";
import MessageBubble from "./MessageBubble";
import TypingIndicator from "./TypingIndicator";

const styles = {
  list: {
    flex: 1,
    minHeight: 0,
    padding: "14px 12px",
    overflowY: "auto",
    background: "#fff7ed",
  },
  loading: {
    margin: "8px 0",
    color: "#92400e",
    fontSize: 13,
  },
};

export default function MessageList({ messages, isTyping, isHistoryLoading }) {
  const { t } = useTranslation();
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isTyping, isHistoryLoading]);

  return (
    <div style={styles.list}>
      {messages.map((message) => (
        <MessageBubble key={message.id} message={message} />
      ))}
      {isHistoryLoading && <p style={styles.loading}>{t("aiWidget.loadingHistory")}</p>}
      {isTyping && <TypingIndicator />}
      <div ref={bottomRef} />
    </div>
  );
}
