import { useAiWidget } from "../../context/AiWidgetContext";

const styles = {
  bar: {
    display: "flex",
    flexDirection: "column",
    alignItems: "stretch",
    gap: 4,
    padding: "8px 12px",
    borderTop: "1px solid rgba(15, 23, 42, 0.08)",
    background: "#fff7ed",
    color: "#9a3412",
    fontSize: 12,
  },
  label: {
    minWidth: 0,
    overflow: "hidden",
    whiteSpace: "nowrap",
    textOverflow: "ellipsis",
    fontWeight: 700,
  },
  row: {
    display: "flex",
    alignItems: "center",
    gap: 8,
  },
  warning: {
    color: "#a16207",
    fontSize: 11,
    lineHeight: 1.35,
  },
  toggle: {
    display: "inline-flex",
    alignItems: "center",
    gap: 6,
    border: "none",
    background: "transparent",
    color: "inherit",
    font: "inherit",
    fontWeight: 800,
    cursor: "pointer",
    padding: 0,
  },
  track: {
    width: 32,
    height: 18,
    borderRadius: 999,
    padding: 2,
    display: "flex",
    alignItems: "center",
  },
  knob: {
    width: 14,
    height: 14,
    borderRadius: "50%",
    background: "#fff",
    boxShadow: "0 1px 3px rgba(15, 23, 42, 0.22)",
  },
};

export default function DocContextBar() {
  const {
    routeDocumentId,
    currentDocTitle,
    docContextEnabled,
    currentDocReady,
    setDocContextEnabled,
  } = useAiWidget();

  if (!routeDocumentId) return null;

  const toggleDisabled = !currentDocReady;

  return (
    <div style={styles.bar}>
      <div style={styles.row}>
        <span style={styles.label}>Hỏi về: {currentDocTitle}</span>
        <button
          type="button"
          style={{
            ...styles.toggle,
            opacity: toggleDisabled ? 0.62 : 1,
            cursor: toggleDisabled ? "not-allowed" : "pointer",
          }}
          onClick={() => setDocContextEnabled((enabled) => !enabled)}
          aria-pressed={docContextEnabled && !toggleDisabled}
          title={docContextEnabled ? "Tắt hỏi theo tài liệu" : "Bật hỏi theo tài liệu"}
          disabled={toggleDisabled}
        >
          <span
            style={{
              ...styles.track,
              justifyContent:
                docContextEnabled && !toggleDisabled ? "flex-end" : "flex-start",
              background:
                docContextEnabled && !toggleDisabled ? "#f97316" : "#d6d3d1",
            }}
          >
            <span style={styles.knob} />
          </span>
          {docContextEnabled && !toggleDisabled ? "ON" : "OFF"}
        </button>
      </div>
      {toggleDisabled && (
        <span style={styles.warning}>
          Tài liệu chưa được nạp cho AI — vào trang chi tiết để nạp
        </span>
      )}
    </div>
  );
}
