import "./ingest-status-badge.css";

const STATUS_BADGES = {
  INGESTED: { className: "ingest-status-badge--ingested", label: "AI ✓" },
  NOT_INGESTED: { className: "ingest-status-badge--pending", label: "Chưa nạp" },
  UNSUPPORTED_FORMAT: { className: "ingest-status-badge--unsupported", label: "Không hỗ trợ" },
};

export default function IngestStatusBadge({ status }) {
  if (!status) return null;

  const badge = STATUS_BADGES[status];
  if (!badge) return null;

  return (
    <span className={`ingest-status-badge ${badge.className}`}>
      {badge.label}
    </span>
  );
}
