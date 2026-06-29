const MODE_LABELS = {
  RAG: "Theo tài liệu",
  SYSTEM: "Hệ thống",
  GENERAL: "Tổng quát",
};

export default function ChatMessage({ role, text, mode }) {
  const isUser = role === "user";
  return (
    <div className={`chat-message ${isUser ? "chat-message--user" : "chat-message--ai"}`}>
      {!isUser && mode && (
        <span className="chat-message__mode">{MODE_LABELS[mode] || mode}</span>
      )}
      <p className="chat-message__text">{text}</p>
    </div>
  );
}
