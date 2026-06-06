import { useState, useRef, useEffect } from "react";
import "./AiShowcaseSection.css";

// =============================================
// DATA — thay bằng props hoặc API call sau này
// =============================================

const SECTION_HEADER = {
  badge: "✦ Thử ngay",
  headline: "Hỏi AI về",
  headlineAccent: "bất kỳ tài liệu nào",
};

const SUGGESTIONS = [
  "Giải thích định lý Pythagore",
  "Tóm tắt chương 1 Giải tích",
  "Python list là gì?",
  "Phân biệt cung và cầu",
];

const AI_SYSTEM_PROMPT = `Bạn là trợ lý AI của "AI Study Hub" — nền tảng học tập dành cho sinh viên Việt Nam.
Nhiệm vụ: giải thích khái niệm học thuật rõ ràng, ngắn gọn, dễ hiểu bằng tiếng Việt.
Luôn trả lời bằng tiếng Việt. Độ dài tối đa 120 từ. Không dùng markdown heading. Có thể dùng danh sách ngắn nếu cần.`;

const INITIAL_MESSAGE = {
  role: "ai",
  text: "Xin chào! Tôi là trợ lý AI của Study Hub. Bạn có thể hỏi tôi về bất kỳ môn học nào, tôi sẽ giải thích ngay cho bạn.",
};

// =============================================

function AiShowcaseSection() {
  const [messages, setMessages] = useState([INITIAL_MESSAGE]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [history, setHistory] = useState([]);
  const chatBodyRef = useRef(null);

  useEffect(() => {
    chatBodyRef.current?.scrollTo({ top: chatBodyRef.current.scrollHeight, behavior: "smooth" });
  }, [messages, loading]);

  async function sendMessage(text) {
    const q = (text ?? input).trim();
    if (!q || loading) return;

    setInput("");
    const userMsg = { role: "user", text: q };
    setMessages((prev) => [...prev, userMsg]);

    const newHistory = [...history, { role: "user", content: q }];
    setHistory(newHistory);
    setLoading(true);

    try {
      const res = await fetch("https://api.anthropic.com/v1/messages", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          model: "claude-sonnet-4-20250514",
          max_tokens: 1000,
          system: AI_SYSTEM_PROMPT,
          messages: newHistory,
        }),
      });
      const data = await res.json();
      const reply =
        data.content?.map((b) => b.text || "").join("") ||
        "Xin lỗi, tôi không thể trả lời lúc này.";

      setMessages((prev) => [...prev, { role: "ai", text: reply }]);
      setHistory((prev) => [...prev, { role: "assistant", content: reply }]);
    } catch {
      setMessages((prev) => [
        ...prev,
        { role: "ai", text: "Có lỗi kết nối. Vui lòng thử lại." },
      ]);
    } finally {
      setLoading(false);
    }
  }

  function handleKey(e) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  }

  return (
    <section id="ai-showcase" className="showcase-section">

      <div className="showcase-header">
        <div>
          <span className="showcase-badge">{SECTION_HEADER.badge}</span>
          <h2 className="showcase-h2">
            {SECTION_HEADER.headline}{" "}
            <span className="showcase-h2__accent">{SECTION_HEADER.headlineAccent}</span>
          </h2>
        </div>
      </div>

      {/* Suggestion chips */}
      <div className="showcase-sugs">
        {SUGGESTIONS.map((s) => (
          <button
            key={s}
            className="sug-btn"
            type="button"
            onClick={() => sendMessage(s)}
            disabled={loading}
          >
            {s}
          </button>
        ))}
      </div>

      {/* Chat shell */}
      <div className="chat-shell">

        <div className="chat-topbar">
          <span className="chat-topbar__dot" aria-hidden="true" />
          <div>
            <div className="chat-topbar__name">AI Study Hub</div>
            <div className="chat-topbar__sub">Trợ lý học tập · Luôn sẵn sàng</div>
          </div>
        </div>

        <div className="chat-body" ref={chatBodyRef} aria-live="polite" aria-label="Cuộc trò chuyện">
          {messages.map((msg, i) => (
            <div key={i} className={`chat-msg chat-msg--${msg.role}`}>
              <div className={`chat-avatar chat-avatar--${msg.role}`} aria-hidden="true">
                {msg.role === "ai" ? "AI" : "B"}
              </div>
              <div className="chat-bubble">{msg.text}</div>
            </div>
          ))}

          {loading && (
            <div className="chat-msg chat-msg--ai" aria-label="AI đang trả lời">
              <div className="chat-avatar chat-avatar--ai" aria-hidden="true">AI</div>
              <div className="chat-bubble chat-bubble--typing">
                <span className="typing-dot" />
                <span className="typing-dot" />
                <span className="typing-dot" />
              </div>
            </div>
          )}
        </div>

        <div className="chat-footer">
          <textarea
            className="chat-input"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKey}
            placeholder="Nhập câu hỏi của bạn..."
            rows={1}
            aria-label="Nhập câu hỏi"
          />
          <button
            className="chat-send"
            onClick={() => sendMessage()}
            disabled={loading || !input.trim()}
            type="button"
            aria-label="Gửi"
          >
            ↑
          </button>
        </div>

      </div>
    </section>
  );
}

export default AiShowcaseSection;