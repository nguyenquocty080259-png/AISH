export default function ChatInput({ value, onChange, onSubmit, sending }) {
  return (
    <form className="chat-input" onSubmit={onSubmit}>
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="Nhập câu hỏi cho AI..."
        disabled={sending}
      />
      <button type="submit" disabled={sending || !value.trim()}>
        {sending ? "Đang gửi..." : "Gửi"}
      </button>
    </form>
  );
}
