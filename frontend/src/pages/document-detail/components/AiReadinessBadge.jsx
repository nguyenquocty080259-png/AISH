import { useTranslation } from "react-i18next";
import Alert from "../../../components/ui/Alert";
import Button from "../../../components/ui/Button";

// Cho biết tài liệu đã sẵn sàng để hỏi AI chưa. Ba trạng thái, mỗi trạng thái một
// intent semantic để màu khớp phần còn lại của app:
//   không đọc được  -> danger (bế tắc thật, chủ tài liệu cũng không làm gì được)
//   chưa nạp        -> warning (chủ tài liệu bấm nạp là xong)
//   đã nạp          -> success
export default function AiReadinessBadge({
  aiSupported,
  ingestStatus,
  isOwner,
  ingesting,
  onIngest,
}) {
  const { t } = useTranslation();

  if (aiSupported === false) {
    return <Alert intent="danger">{t("docDetail.aiUnsupportedFormat")}</Alert>;
  }

  if (ingestStatus === "UNSUPPORTED_FORMAT") {
    return <Alert intent="danger">{t("docDetail.aiNoExtract")}</Alert>;
  }

  if (ingestStatus === "INGESTED") {
    return <Alert intent="success">{t("docDetail.aiReady")}</Alert>;
  }

  return (
    <Alert
      intent="warning"
      action={
        isOwner && (
          <Button size="sm" variant="secondary" onClick={onIngest} loading={ingesting}>
            {ingesting ? t("docDetail.aiIngesting") : t("docDetail.aiIngest")}
          </Button>
        )
      }
    >
      {t("docDetail.aiNotIngested")}
    </Alert>
  );
}
