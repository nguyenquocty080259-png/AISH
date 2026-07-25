import { useTranslation } from "react-i18next";
import "./ingest-status-badge.css";

const STATUS_META = {
  INGESTED: { className: "ingest-status-badge--ingested", labelKey: "admin.ingest.ingested" },
  NOT_INGESTED: { className: "ingest-status-badge--pending", labelKey: "admin.ingest.notIngested" },
  UNSUPPORTED_FORMAT: { className: "ingest-status-badge--unsupported", labelKey: "admin.ingest.unsupported" },
};

export default function IngestStatusBadge({ status }) {
  const { t } = useTranslation();
  if (!status) return null;

  const badge = STATUS_META[status];
  if (!badge) return null;

  return (
    <span className={`ingest-status-badge ${badge.className}`}>
      {t(badge.labelKey)}
    </span>
  );
}
