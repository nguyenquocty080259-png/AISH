import { useTranslation } from "react-i18next";
import Badge from "./Badge";

// Bảng màu DUY NHẤT cho trạng thái kiểm duyệt và phạm vi hiển thị của tài liệu.
// Mọi nơi (Cộng đồng, Tài liệu của tôi, Chi tiết, Quản trị) đều dùng chung để một
// trạng thái luôn ra đúng một màu.
//   ADMIN_PENDING -> warning (chờ Admin duyệt, nối tiếp luồng ở backend)
//   APPROVED      -> success
//   REJECTED      -> danger
//   NOT_REQUIRED  -> neutral (tài liệu riêng tư, chưa cần duyệt)
const MODERATION = {
  NOT_REQUIRED: { intent: "neutral", key: "common.moderation.notRequired" },
  ADMIN_PENDING: { intent: "warning", key: "common.moderation.adminPending", dot: true },
  APPROVED: { intent: "success", key: "common.moderation.approved" },
  REJECTED: { intent: "danger", key: "common.moderation.rejected" },
};

const VISIBILITY = {
  PUBLIC: { intent: "info", key: "common.visibility.public" },
  PRIVATE: { intent: "neutral", key: "common.visibility.private" },
};

// status: "NOT_REQUIRED" | "ADMIN_PENDING" | "APPROVED" | "REJECTED".
// Giá trị lạ / rỗng -> không render gì, tránh badge trống.
export function ModerationBadge({ status, size, className = "" }) {
  const { t } = useTranslation();
  const cfg = MODERATION[status];
  if (!cfg) return null;
  return (
    <Badge intent={cfg.intent} size={size} dot={cfg.dot} className={className}>
      {t(cfg.key)}
    </Badge>
  );
}

// visibility: "PUBLIC" | "PRIVATE".
export function VisibilityBadge({ visibility, size, className = "" }) {
  const { t } = useTranslation();
  const cfg = VISIBILITY[visibility];
  if (!cfg) return null;
  return (
    <Badge intent={cfg.intent} size={size} className={className}>
      {t(cfg.key)}
    </Badge>
  );
}

export default ModerationBadge;
