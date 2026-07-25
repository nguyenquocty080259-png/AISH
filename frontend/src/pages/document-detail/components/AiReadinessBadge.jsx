const styles = {
  badge: {
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    gap: 12,
    padding: "12px 14px",
    borderRadius: 10,
    border: "1px solid rgba(245, 124, 0, 0.24)",
    background: "rgba(245, 124, 0, 0.08)",
    color: "#92400e",
    margin: "16px 0",
    fontSize: 14,
  },
  ready: {
    borderColor: "rgba(46, 125, 50, 0.24)",
    background: "rgba(46, 125, 50, 0.08)",
    color: "#166534",
  },
  unsupported: {
    borderColor: "rgba(220, 38, 38, 0.18)",
    background: "rgba(220, 38, 38, 0.08)",
    color: "#991b1b",
  },
  text: {
    margin: 0,
    lineHeight: 1.45,
  },
};

import { useTranslation } from "react-i18next";

export default function AiReadinessBadge({
  aiSupported,
  ingestStatus,
  isOwner,
  ingesting,
  onIngest,
}) {
  const { t } = useTranslation();
  if (aiSupported === false) {
    return (
      <div style={{ ...styles.badge, ...styles.unsupported }}>
        <p style={styles.text}>{t("docDetail.aiUnsupportedFormat")}</p>
      </div>
    );
  }

  if (ingestStatus === "UNSUPPORTED_FORMAT") {
    return (
      <div style={{ ...styles.badge, ...styles.unsupported }}>
        <p style={styles.text}>
          {t("docDetail.aiNoExtract")}
        </p>
      </div>
    );
  }

  if (ingestStatus === "INGESTED") {
    return (
      <div style={{ ...styles.badge, ...styles.ready }}>
        <p style={styles.text}>{t("docDetail.aiReady")}</p>
      </div>
    );
  }

  return (
    <div style={styles.badge}>
      <p style={styles.text}>{t("docDetail.aiNotIngested")}</p>
      {isOwner && (
        <button
          type="button"
          className="detail-btn"
          onClick={onIngest}
          disabled={ingesting}
        >
          {ingesting ? t("docDetail.aiIngesting") : t("docDetail.aiIngest")}
        </button>
      )}
    </div>
  );
}
