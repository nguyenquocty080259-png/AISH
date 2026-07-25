import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ROUTES, buildRoute } from "../../../constants/routes";

// DEC-028: UI phải phân biệt rõ RAG (trả lời từ tài liệu) và GENERAL (kiến thức chung),
// không để người dùng nhầm là AI luôn đọc tài liệu của họ. Nhãn hiển thị lấy qua i18n.
const MODE_KEYS = {
  RAG: "aiChat.modeRag",
  GENERAL: "aiChat.modeGeneral",
};

// Cầu nối citation -> viewer: truyền qua query param (page, highlight) thay vì route state,
// vì citation luôn mở ở TAB MỚI (giữ nguyên hội thoại chat) — route state không đi qua được
// target="_blank", còn query param thì luôn hoạt động và có thể chia sẻ/đánh dấu trang.
function citationHref(citation) {
  const params = new URLSearchParams();
  if (citation.page != null) params.set("page", citation.page);
  if (citation.snippet) params.set("highlight", citation.snippet);
  const query = params.toString();
  const base = buildRoute(ROUTES.DOCUMENT_DETAIL, { id: citation.documentId });
  return query ? `${base}?${query}` : base;
}

export default function ChatMessage({
  role,
  text,
  mode,
  citations = [],
  relatedDocs = [],
}) {
  const { t } = useTranslation();
  const isUser = role === "user";
  const hasCitations = mode === "RAG" && citations.length > 0;
  const hasRelated = relatedDocs.length > 0;

  return (
    <div className={`chat-message ${isUser ? "chat-message--user" : "chat-message--ai"}`}>
      {!isUser && mode && (
        <span className={`chat-message__mode chat-message__mode--${mode.toLowerCase()}`}>
          {MODE_KEYS[mode] ? t(MODE_KEYS[mode]) : mode}
        </span>
      )}

      <p className="chat-message__text">{text}</p>

      {hasCitations && (
        <div className="chat-citations">
          <span className="chat-citations__title">{t("aiChat.citationsTitle")}</span>
          {citations.map((c, i) =>
            c.documentId != null ? (
              <Link
                to={citationHref(c)}
                target="_blank"
                rel="noreferrer"
                className="chat-citation"
                key={`${c.documentId}-${c.page}-${i}`}
                title={t("aiChat.citationOpenHint")}
              >
                <div className="chat-citation__head">
                  <span className="chat-citation__doc">{c.title}</span>
                  {c.author && (
                    <span className="chat-citation__author">{c.author}</span>
                  )}
                  {c.page != null && (
                    <span className="chat-citation__page">{t("aiChat.page", { page: c.page })}</span>
                  )}
                </div>
                <p className="chat-citation__snippet">{c.snippet}</p>
              </Link>
            ) : (
              <div className="chat-citation" key={`no-doc-${i}`}>
                <div className="chat-citation__head">
                  <span className="chat-citation__doc">{c.title}</span>
                  {c.page != null && (
                    <span className="chat-citation__page">{t("aiChat.page", { page: c.page })}</span>
                  )}
                </div>
                <p className="chat-citation__snippet">{c.snippet}</p>
              </div>
            )
          )}
        </div>
      )}

      {/* DEC-027: gợi ý tài liệu liên quan tách riêng khỏi câu trả lời chính.
          Backend hiện luôn trả [] ở MVP nên đây là hiển thị dự phòng, chưa có shape cuối cùng. */}
      {hasRelated && (
        <div className="chat-related">
          <span className="chat-related__title">{t("aiChat.relatedTitle")}</span>
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
