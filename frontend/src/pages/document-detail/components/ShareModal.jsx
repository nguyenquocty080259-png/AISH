import { useState } from "react";
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
  const { showSuccess, showError } = useToast();
  const [mode, setMode] = useState("RESTRICTED");
  const [email, setEmail] = useState("");
  const [permission, setPermission] = useState("COMMENTER");
  const [shareLink, setShareLink] = useState("");
  const [revokingId, setRevokingId] = useState(null);

  const shareUrl = shareLink
    ? `${window.location.origin}${documentDetailPath}`
    : "";

  const permissionLabel = (p) => (p === "COMMENTER" ? "Bình luận" : "Xem");

  const submit = async (e) => {
    e.preventDefault();
    setShareLink("");

    if (mode === "RESTRICTED") {
      const trimmed = email.trim();
      if (!trimmed) {
        showError("Nhập email người nhận (đã có tài khoản HiveMind).");
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
      showSuccess("Đã copy liên kết.");
    } catch {
      showError("Không copy được liên kết.");
    }
  };

  return (
    <Modal open={open} onClose={onClose} title="Chia sẻ tài liệu">
      <form className="detail-share" onSubmit={submit}>
        <label className="detail-share__field">
          <span>Chế độ chia sẻ</span>
          <select value={mode} onChange={(e) => setMode(e.target.value)}>
            <option value="RESTRICTED">Chỉ người được mời</option>
            <option value="ANYONE_WITH_LINK">Bất kỳ ai có liên kết</option>
            <option value="NONE">Tắt chia sẻ qua liên kết</option>
          </select>
        </label>

        {mode === "RESTRICTED" && (
          <label className="detail-share__field">
            <span>Email người nhận</span>
            <input
              type="email"
              placeholder="VD: ban@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </label>
        )}

        {mode !== "NONE" && (
          <label className="detail-share__field">
            <span>Quyền</span>
            <select value={permission} onChange={(e) => setPermission(e.target.value)}>
              <option value="VIEWER">Xem (xem, tải, hỏi AI)</option>
              <option value="COMMENTER">Bình luận (xem + bình luận)</option>
            </select>
          </label>
        )}

        <div className="detail-share__actions">
          <Button type="submit" variant="primary" disabled={sharing}>
            {sharing ? "Đang xử lý..." : "Áp dụng"}
          </Button>
        </div>
      </form>

      {mode === "RESTRICTED" && (
        <div className="detail-share__recipients">
          <p className="detail-share__recipients-title">Đang chia sẻ cho</p>
          {loadingRecipients ? (
            <p className="detail-share__recipients-empty">Đang tải...</p>
          ) : recipients.length === 0 ? (
            <p className="detail-share__recipients-empty">Chưa chia sẻ cho ai.</p>
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
                    {revokingId === r.userId ? "Đang gỡ..." : "Gỡ"}
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
            Copy
          </Button>
        </div>
      )}
    </Modal>
  );
}
