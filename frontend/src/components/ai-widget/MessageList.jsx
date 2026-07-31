import { useEffect, useRef } from "react";
import { useTranslation } from "react-i18next";
import MessageBubble from "./MessageBubble";
import TypingIndicator from "./TypingIndicator";
import "./ai-widget.css";

export default function MessageList({ messages, isTyping, isHistoryLoading }) {
  const { t } = useTranslation();
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isTyping, isHistoryLoading]);

  return (
    <div className="ai-widget__list">
      {messages.map((message) => (
        <MessageBubble key={message.id} message={message} />
      ))}
      {isHistoryLoading && <p className="ai-widget__list-note">{t("aiWidget.loadingHistory")}</p>}
      {isTyping && <TypingIndicator />}
      <div ref={bottomRef} />
    </div>
  );
}
