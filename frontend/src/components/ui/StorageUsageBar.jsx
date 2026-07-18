import "./StorageUsageBar.css";

// Dùng chung cho My Documents, Hồ sơ và UploadModal - không viết lại 3 lần.
export function formatBytes(bytes) {
  const value = Number(bytes) || 0;
  const gb = value / (1024 * 1024 * 1024);
  if (gb >= 1) return `${gb.toFixed(1)} GB`;
  const mb = value / (1024 * 1024);
  if (mb >= 1) return `${mb.toFixed(1)} MB`;
  return `${(value / 1024).toFixed(1)} KB`;
}

function levelFor(pct) {
  if (pct >= 90) return "danger";
  if (pct >= 70) return "warning";
  return "normal";
}

function UsageRow({ label, used, quota }) {
  const pct = quota > 0 ? Math.min(100, (used / quota) * 100) : 0;
  return (
    <div className="storage-usage-bar__row">
      <div className="storage-usage-bar__row-label">
        <span>{label}</span>
        <span>
          đã dùng {formatBytes(used)} / {formatBytes(quota)}
        </span>
      </div>
      <div className="storage-usage-bar__track">
        <div
          className={`storage-usage-bar__fill storage-usage-bar__fill--${levelFor(pct)}`}
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  );
}

// usage: StorageUsageDTO { usedLocalBytes, usedCloudBytes, quotaLocalBytes, quotaCloudBytes, ... } | null.
// null (chưa tải xong / lỗi) -> không render gì, không phải lỗi hiển thị.
export default function StorageUsageBar({ usage, className = "" }) {
  if (!usage) return null;

  return (
    <div className={`storage-usage-bar ${className}`.trim()}>
      <UsageRow label="Máy chủ (LOCAL)" used={usage.usedLocalBytes} quota={usage.quotaLocalBytes} />
      <UsageRow label="Cloud (CLOUD)" used={usage.usedCloudBytes} quota={usage.quotaCloudBytes} />
      <p className="storage-usage-bar__hint">
        Tài liệu trong thùng rác vẫn tính vào dung lượng.
      </p>
    </div>
  );
}
