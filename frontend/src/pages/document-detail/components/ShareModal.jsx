import { useState } from "react";
import { useTranslation } from "react-i18next";
import Modal from "../../../components/ui/Modal";
import Button from "../../../components/ui/Button";
import { useToast } from "../../../hooks/useToast";

// Modal chia sẻ tài liệu cho chủ sở hữu.
// - RESTRICTED: mời người dùng theo EMAIL (người nhận phải đã có tài khoản HiveMind) + chọn quyền,
//   kèm danh sách người đang được chia sẻ và nút Gỡ tại từng dòng.
// - ANYONE_WITH_LINK: bật link chia sẻ, hiển thị link để copy.
// - NONE: tắt link chia sẻ.
// Quyền V1: VIEWER (xem/tải/hỏi AI) hoặc COMMENTER (thêm bình luận). EDITOR để dành V2.
export default function ShareModal({
  open,
  onClose,
  onShare,
  sharing,
  documentDetailPath,
  recipients = [],
  loadingRecipients = false,
  onRevoke,
}) {
  const { t } = useTranslation();
  const { showSuccess, showError } = useToast();
  const [mode, setMode] = useState("RESTRICTED");
  const [email, setEmail] = useState("");
  const [permission, setPermission] = useState("COMMENTER");
  const [shareLink, setShareLink] = useState("");
  const [revokingId, setRevokingId] = useState(null);

  const shareUrl = shareLink
    ? `${window.location.origin}${documentDetailPath}`
    : "";

  const permissionLabel = (p) => (p === "COMMENTER" ? t("docDetail.share_modal.permCommenter") : t("docDetail.share_modal.permViewer"));

  const submit = async (e) => {
    e.preventDefault();
    setShareLink("");

    if (mode === "RESTRICTED") {
      const trimmed = email.trim();
      if (!trimmed) {
        showError(t("docDetail.share_modal.needEmail"));
        return;
      }
      const res = await onShare({ mode, email: trimmed, permission });
      if (res) setEmail(""); // chỉ xoá ô nhập khi chia sẻ thành công
    } else if (mode === "ANYONE_WITH_LINK") {
      const res = await onShare({ mode, permission });
      if (res?.shareToken) setShareLink(res.shareToken);
    } else {
      await onShare({ mode: "NONE" });
    }
  };

  const handleRevoke = async (userId) => {
    setRevokingId(userId);
    try {
      await onRevoke(userId);
    } finally {
      setRevokingId(null);
    }
  };

  const copyLink = async () => {
    try {
      await navigator.clipboard.writeText(shareUrl);
      showSuccess(t("docDetail.share_modal.copied"));
    } catch {
      showError(t("docDetail.share_modal.copyError"));
    }
  };

  return (
    <Modal open={open} onClose={onClose} title={t("docDetail.share_modal.title")}>
      <form className="detail-share" onSubmit={submit}>
        <label className="detail-share__field">
          <span>{t("docDetail.share_modal.modeLabel")}</span>
          <select value={mode} onChange={(e) => setMode(e.target.value)}>
            <option value="RESTRICTED">{t("docDetail.share_modal.modeRestricted")}</option>
            <option value="ANYONE_WITH_LINK">{t("docDetail.share_modal.modeAnyone")}</option>
            <option value="NONE">{t("docDetail.share_modal.modeNone")}</option>
          </select>
        </label>

        {mode === "RESTRICTED" && (
          <label className="detail-share__field">
            <span>{t("docDetail.share_modal.emailLabel")}</span>
            <input
              type="email"
              placeholder={t("docDetail.share_modal.emailPlaceholder")}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </label>
        )}

        {mode !== "NONE" && (
          <label className="detail-share__field">
            <span>{t("docDetail.share_modal.permLabel")}</span>
            <select value={permission} onChange={(e) => setPermission(e.target.value)}>
              <option value="VIEWER">{t("docDetail.share_modal.permViewerOpt")}</option>
              <option value="COMMENTER">{t("docDetail.share_modal.permCommenterOpt")}</option>
            </select>
          </label>
        )}

        <div className="detail-share__actions">
          <Button type="submit" variant="primary" disabled={sharing}>
            {sharing ? t("docDetail.share_modal.processing") : t("docDetail.share_modal.apply")}
          </Button>
        </div>
      </form>

      {mode === "RESTRICTED" && (
        <div className="detail-share__recipients">
          <p className="detail-share__recipients-title">{t("docDetail.share_modal.sharingTo")}</p>
          {loadingRecipients ? (
            <p className="detail-share__recipients-empty">{t("docDetail.share_modal.loadingRecipients")}</p>
          ) : recipients.length === 0 ? (
            <p className="detail-share__recipients-empty">{t("docDetail.share_modal.noRecipients")}</p>
          ) : (
            <ul className="detail-share__recipients-list">
              {recipients.map((r) => (
                <li key={r.userId} className="detail-share__recipient">
                  <span className="detail-share__recipient-info">
                    <strong>{r.fullName || r.email || `#${r.userId}`}</strong>
                    {r.email && <span className="detail-share__recipient-email">{r.email}</span>}
                    <span className="detail-share__recipient-perm">{permissionLabel(r.permission)}</span>
                  </span>
                  <Button
                    type="button"
                    variant="secondary"
                    disabled={revokingId === r.userId}
                    onClick={() => handleRevoke(r.userId)}
                  >
                    {revokingId === r.userId ? t("docDetail.share_modal.revoking") : t("docDetail.share_modal.revoke")}
                  </Button>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {shareLink && mode === "ANYONE_WITH_LINK" && (
        <div className="detail-share__link">
          <input type="text" readOnly value={shareUrl} onFocus={(e) => e.target.select()} />
          <Button type="button" variant="secondary" onClick={copyLink}>
            {t("docDetail.share_modal.copy")}
          </Button>
        </div>
      )}
    </Modal>
  );
}
