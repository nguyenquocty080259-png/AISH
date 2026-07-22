import { useState } from "react";
import Modal from "../../../components/ui/Modal";
import Button from "../../../components/ui/Button";
import { useToast } from "../../../hooks/useToast";

// Modal chia sẻ tài liệu cho chủ sở hữu.
// - RESTRICTED: mời user cụ thể theo ID (V1 chưa có endpoint tìm user nên nhập ID, cách nhau dấu phẩy).
// - ANYONE_WITH_LINK: bật link chia sẻ, hiển thị link để copy.
// - NONE: tắt link chia sẻ.
// Quyền V1: VIEWER (xem/tải/hỏi AI) hoặc COMMENTER (thêm bình luận). EDITOR để dành V2.
export default function ShareModal({ open, onClose, onShare, sharing, documentDetailPath }) {
  const { showSuccess, showError } = useToast();
  const [mode, setMode] = useState("RESTRICTED");
  const [userIdsText, setUserIdsText] = useState("");
  const [permission, setPermission] = useState("COMMENTER");
  const [shareLink, setShareLink] = useState("");

  const shareUrl = shareLink
    ? `${window.location.origin}${documentDetailPath}`
    : "";

  const parseUserIds = () =>
    userIdsText
      .split(",")
      .map((x) => Number(x.trim()))
      .filter((x) => Number.isInteger(x) && x > 0);

  const submit = async (e) => {
    e.preventDefault();
    setShareLink("");

    if (mode === "RESTRICTED") {
      const userIds = parseUserIds();
      if (userIds.length === 0) {
        showError("Nhập ít nhất một ID người dùng hợp lệ (cách nhau dấu phẩy).");
        return;
      }
      await onShare({ mode, userIds, permission });
    } else if (mode === "ANYONE_WITH_LINK") {
      const res = await onShare({ mode, permission });
      if (res?.shareToken) setShareLink(res.shareToken);
    } else {
      await onShare({ mode: "NONE" });
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
            <span>ID người dùng (cách nhau dấu phẩy)</span>
            <input
              type="text"
              placeholder="VD: 12, 34, 56"
              value={userIdsText}
              onChange={(e) => setUserIdsText(e.target.value)}
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
