const getRowStyle = (isUser) => ({
  display: "flex",
  justifyContent: isUser ? "flex-end" : "flex-start",
  margin: "8px 0",
});

const getBubbleStyle = (isUser, isError) => ({
  maxWidth: "82%",
  padding: "9px 12px",
  borderRadius: isUser ? "16px 16px 4px 16px" : "16px 16px 16px 4px",
  background: isUser ? "#f97316" : isError ? "#fee2e2" : "#fff",
  color: isUser ? "#fff" : isError ? "#991b1b" : "#1f2937",
  boxShadow: "0 4px 14px rgba(15, 23, 42, 0.08)",
  border: isUser ? "none" : "1px solid rgba(15, 23, 42, 0.06)",
  whiteSpace: "pre-wrap",
  overflowWrap: "anywhere",
  fontSize: 14,
  lineHeight: 1.45,
});

export default function MessageBubble({ message }) {
  const isUser = message.role === "user";

  return (
    <div style={getRowStyle(isUser)}>
      <div style={getBubbleStyle(isUser, message.isError)}>{message.text}</div>
    </div>
  );
}
