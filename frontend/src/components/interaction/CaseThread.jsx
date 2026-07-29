import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { getCaseMessages, postCaseMessage } from "../../api/caseApi";
import { useAuth } from "../../hooks/useAuth";
import { useToast } from "../../hooks/useToast";
import Button from "../ui/Button";
import "./case-thread.css";

// caseType: "report" | "appeal". caseId: id của report/appeal tương ứng.
// user.id không được /api/auth/me trả về, nên căn tin "của mình" theo role hiện tại
// (đủ dùng vì mỗi thread chỉ có 1 chủ case + admin trao đổi qua lại).
export default function CaseThread({ caseType, caseId }) {
  const { t } = useTranslation();
  const { role } = useAuth();
  const { showError } = useToast();
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [content, setContent] = useState("");
  const [sending, setSending] = useState(false);

  const loadMessages = useCallback(() => {
    setLoading(true);
    return getCaseMessages(caseType, caseId)
      .then(setMessages)
      .catch((error) => showError(error.message || t("caseThread.loadError")))
      .finally(() => setLoading(false));
  }, [caseType, caseId, showError, t]);

  useEffect(() => {
    loadMessages();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [caseType, caseId]);

  const handleSend = () => {
    const trimmed = content.trim();
    if (!trimmed || sending) return;
    setSending(true);
    postCaseMessage(caseType, caseId, trimmed)
      .then(() => {
        setContent("");
        return loadMessages();
      })
      .catch((error) => showError(error.message || t("caseThread.sendError")))
      .finally(() => setSending(false));
  };

  const roleLabel = (senderRole) =>
    senderRole?.toUpperCase() === "ADMIN" ? t("caseThread.roleAdmin") : t("caseThread.roleUser");

  return (
    <div className="case-thread">
      <div className="case-thread__messages">
        {loading ? (
          <p className="case-thread__status">{t("caseThread.loading")}</p>
        ) : messages.length === 0 ? (
          <p className="case-thread__status">{t("caseThread.empty")}</p>
        ) : (
          messages.map((message) => {
            const isMine = message.senderRole?.toUpperCase() === role?.toUpperCase();
            return (
              <div
                key={message.id}
                className={`case-thread__message ${isMine ? "case-thread__message--mine" : ""}`.trim()}
              >
                <div className="case-thread__message-header">
                  <span className="case-thread__sender">{message.senderName || roleLabel(message.senderRole)}</span>
                  <span className={`case-thread__role-badge case-thread__role-badge--${message.senderRole?.toLowerCase()}`}>
                    {roleLabel(message.senderRole)}
                  </span>
                </div>
                <p className="case-thread__content">{message.content}</p>
                <span className="case-thread__time">
                  {message.createdAt ? new Date(message.createdAt).toLocaleString() : ""}
                </span>
              </div>
            );
          })
        )}
      </div>
      <div className="case-thread__composer">
        <textarea
          rows={3}
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder={t("caseThread.placeholder")}
          disabled={sending}
        />
        <Button onClick={handleSend} disabled={sending || !content.trim()}>
          {sending ? t("caseThread.sending") : t("caseThread.send")}
        </Button>
      </div>
    </div>
  );
}
