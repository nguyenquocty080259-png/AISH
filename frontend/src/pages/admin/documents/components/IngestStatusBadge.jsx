import { useTranslation } from "react-i18next";
import Badge from "../../../../components/ui/Badge";

// Trạng thái nạp nội dung cho AI. Dùng Badge chung để cùng bảng màu semantic với
// các badge khác (đã nạp = success, chưa nạp = neutral, không đọc được = warning).
const STATUS_META = {
  INGESTED: { intent: "success", labelKey: "admin.ingest.ingested" },
  NOT_INGESTED: { intent: "neutral", labelKey: "admin.ingest.notIngested" },
  UNSUPPORTED_FORMAT: { intent: "warning", labelKey: "admin.ingest.unsupported" },
};

// Nhãn nhỏ hiện trạng thái nạp nội dung tài liệu cho AI (đã nạp/chưa nạp/không đọc được).
export default function IngestStatusBadge({ status }) {
  const { t } = useTranslation();
  if (!status) return null;

  const badge = STATUS_META[status];
  if (!badge) return null;

  return <Badge intent={badge.intent}>{t(badge.labelKey)}</Badge>;
}
