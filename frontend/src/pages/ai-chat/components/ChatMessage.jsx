// DEC-028: UI phải phân biệt rõ RAG (trả lời từ tài liệu) và GENERAL (kiến thức chung),
// không để người dùng nhầm là AI luôn đọc tài liệu của họ.
const MODE_DESCRIPTIONS = {
  RAG: "Trả lời từ tài liệu của bạn",
  GENERAL: "Trả lời từ kiến thức chung, không từ tài liệu của bạn",
};

export default function ChatMessage({
  role,
  text,
  mode,
  citations = [],
  relatedDocs = [],
}) {
  const isUser = role === "user";
  const hasCitations = mode === "RAG" && citations.length > 0;
  const hasRelated = relatedDocs.length > 0;

  return (
    <div className={`chat-message ${isUser ? "chat-message--user" : "chat-message--ai"}`}>
      {!isUser && mode && (
        <span className={`chat-message__mode chat-message__mode--${mode.toLowerCase()}`}>
          {MODE_DESCRIPTIONS[mode] || mode}
        </span>
      )}

      <p className="chat-message__text">{text}</p>

      {hasCitations && (
        <div className="chat-citations">
          <span className="chat-citations__title">Nguồn trích dẫn</span>
          {citations.map((c, i) => (
            <div className="chat-citation" key={`${c.documentId}-${c.page}-${i}`}>
              <div className="chat-citation__head">
                <span className="chat-citation__doc">{c.documentTitle}</span>
                {c.page != null && (
                  <span className="chat-citation__page">Trang {c.page}</span>
                )}
              </div>
              <p className="chat-citation__snippet">{c.snippet}</p>
            </div>
          ))}
        </div>
      )}

      {/* DEC-027: gợi ý tài liệu liên quan tách riêng khỏi câu trả lời chính.
          Backend hiện luôn trả [] ở MVP nên đây là hiển thị dự phòng, chưa có shape cuối cùng. */}
      {hasRelated && (
        <div className="chat-related">
          <span className="chat-related__title">Tài liệu liên quan</span>
          {relatedDocs.map((d, i) => (
            <span className="chat-related__item" key={i}>
              {d?.title || d?.documentTitle || String(d)}
            </span>
          ))}
        </div>
      )}
    </div>
  );
}
