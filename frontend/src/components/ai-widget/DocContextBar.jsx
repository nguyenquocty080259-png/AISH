import { useAiWidget } from "../../context/AiWidgetContext";

const styles = {
  bar: {
    display: "flex",
    alignItems: "center",
    gap: 8,
    padding: "8px 12px",
    borderTop: "1px solid rgba(15, 23, 42, 0.08)",
    background: "#fff7ed",
    color: "#9a3412",
    fontSize: 12,
  },
  label: {
    flex: 1,
    minWidth: 0,
    overflow: "hidden",
    whiteSpace: "nowrap",
    textOverflow: "ellipsis",
    fontWeight: 700,
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
    setDocContextEnabled,
  } = useAiWidget();

  if (!routeDocumentId) return null;

  return (
    <div style={styles.bar}>
      <span style={styles.label}>Hỏi về: {currentDocTitle}</span>
      <button
        type="button"
        style={styles.toggle}
        onClick={() => setDocContextEnabled((enabled) => !enabled)}
        aria-pressed={docContextEnabled}
        title={docContextEnabled ? "Tắt hỏi theo tài liệu" : "Bật hỏi theo tài liệu"}
      >
        <span
          style={{
            ...styles.track,
            justifyContent: docContextEnabled ? "flex-end" : "flex-start",
            background: docContextEnabled ? "#f97316" : "#d6d3d1",
          }}
        >
          <span style={styles.knob} />
        </span>
        {docContextEnabled ? "ON" : "OFF"}
      </button>
    </div>
  );
}
