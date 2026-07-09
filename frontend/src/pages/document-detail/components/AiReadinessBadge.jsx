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

export default function AiReadinessBadge({
  aiSupported,
  ingestStatus,
  isOwner,
  ingesting,
  onIngest,
}) {
  if (aiSupported === false) {
    return (
      <div style={{ ...styles.badge, ...styles.unsupported }}>
        <p style={styles.text}>AI chưa đọc được định dạng này</p>
      </div>
    );
  }

  if (ingestStatus === "UNSUPPORTED_FORMAT") {
    return (
      <div style={{ ...styles.badge, ...styles.unsupported }}>
        <p style={styles.text}>
          Không trích xuất được nội dung cho AI (file có thể là bản scan/không
          có lớp chữ)
        </p>
      </div>
    );
  }

  if (ingestStatus === "INGESTED") {
    return (
      <div style={{ ...styles.badge, ...styles.ready }}>
        <p style={styles.text}>✅ Đã sẵn sàng cho AI</p>
      </div>
    );
  }

  return (
    <div style={styles.badge}>
      <p style={styles.text}>⚠️ Chưa nạp cho AI</p>
      {isOwner && (
        <button
          type="button"
          className="detail-btn"
          onClick={onIngest}
          disabled={ingesting}
        >
          {ingesting ? "Đang nạp..." : "Nạp cho AI"}
        </button>
      )}
    </div>
  );
}
