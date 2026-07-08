import { useAiWidget } from "../../context/AiWidgetContext";

const styles = {
  header: {
    minHeight: 68,
    padding: "12px 14px",
    background: "linear-gradient(135deg, #f59e0b, #f97316)",
    color: "#fff",
    display: "flex",
    alignItems: "center",
    gap: 10,
  },
  avatar: {
    width: 40,
    height: 40,
    borderRadius: "50%",
    background: "rgba(255, 255, 255, 0.22)",
    border: "1px solid rgba(255, 255, 255, 0.42)",
    display: "grid",
    placeItems: "center",
    fontWeight: 800,
    fontSize: 14,
    flexShrink: 0,
  },
  meta: {
    flex: 1,
    minWidth: 0,
  },
  name: {
    margin: 0,
    fontSize: 15,
    fontWeight: 800,
    letterSpacing: 0,
  },
  status: {
    margin: "2px 0 0",
    fontSize: 12,
    opacity: 0.9,
  },
  actions: {
    display: "flex",
    gap: 6,
  },
  action: {
    width: 30,
    height: 30,
    borderRadius: "50%",
    border: "1px solid rgba(255, 255, 255, 0.34)",
    color: "#fff",
    background: "rgba(255, 255, 255, 0.16)",
    cursor: "pointer",
    fontSize: 18,
    lineHeight: "28px",
  },
};

export default function ChatHeader() {
  const { closeWidget } = useAiWidget();

  return (
    <header style={styles.header}>
      <div style={styles.avatar}>AI</div>
      <div style={styles.meta}>
        <p style={styles.name}>AI HiveMind</p>
        <p style={styles.status}>Online</p>
      </div>
      <div style={styles.actions}>
        <button type="button" style={styles.action} onClick={closeWidget} title="Thu nhỏ">
          -
        </button>
        <button type="button" style={styles.action} onClick={closeWidget} title="Đóng">
          ×
        </button>
      </div>
    </header>
  );
}
