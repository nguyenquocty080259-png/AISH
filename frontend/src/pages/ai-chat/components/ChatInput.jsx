export default function ChatInput({
  value,
  onChange,
  onSubmit,
  sending,
  disabled = false,
}) {
  const isDisabled = sending || disabled;

  return (
    <form className="chat-input" onSubmit={onSubmit}>
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="Nhập câu hỏi cho AI HiveMind..."
        disabled={isDisabled}
      />
      <button type="submit" disabled={isDisabled || !value.trim()}>
        {sending ? "Đang gửi..." : "Gửi"}
      </button>
    </form>
  );
}
