import "./ai-widget.css";

// Một bong bóng tin nhắn trong widget (của user, AI, hoặc AI báo lỗi).
export default function MessageBubble({ message }) {
  const isUser = message.role === "user";
  const bubbleClass = [
    "ai-widget__bubble",
    isUser ? "ai-widget__bubble--user" : "",
    message.isError ? "ai-widget__bubble--error" : "",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <div className={`ai-widget__row ${isUser ? "ai-widget__row--user" : ""}`.trim()}>
      <div className={bubbleClass}>{message.text}</div>
    </div>
  );
}
