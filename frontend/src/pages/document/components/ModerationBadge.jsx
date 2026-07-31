import { useTranslation } from "react-i18next";
import Badge from "../../../components/ui/Badge";

// Nhãn "đã kiểm duyệt" cho tài liệu ĐANG công khai — cho biết mới qua AI hay đã có Admin
// duyệt. Trạng thái kiểm duyệt thô (ADMIN_PENDING/APPROVED/REJECTED) hiển thị bằng
// components/ui/StatusBadge để dùng chung bảng màu.
export default function ModerationBadge({ doc }) {
  const { t } = useTranslation();
  if (doc?.visibility !== "PUBLIC" || doc?.moderationStatus !== "APPROVED") {
    return null;
  }

  const adminReviewed = doc.adminReviewedAt != null;

  return (
    <Badge intent={adminReviewed ? "success" : "warning"} size="sm">
      {adminReviewed ? t("documents.moderationAiAdmin") : t("documents.moderationAi")}
    </Badge>
  );
}
