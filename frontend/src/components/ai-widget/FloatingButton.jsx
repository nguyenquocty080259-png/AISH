import { useTranslation } from "react-i18next";
import { useAiWidget } from "../../context/AiWidgetContext";

const styles = {
  button: {
    position: "fixed",
    right: 24,
    bottom: 24,
    width: 56,
    height: 56,
    borderRadius: "50%",
    border: "none",
    background: "linear-gradient(135deg, #f59e0b, #f97316)",
    color: "#fff",
    boxShadow: "0 14px 34px rgba(249, 115, 22, 0.36)",
    cursor: "pointer",
    zIndex: 9999,
    display: "grid",
    placeItems: "center",
    transition: "transform 160ms ease, box-shadow 160ms ease",
  },
  icon: {
    fontSize: 24,
    lineHeight: 1,
  },
  badge: {
    position: "absolute",
    top: -4,
    right: -4,
    minWidth: 20,
    height: 20,
    padding: "0 6px",
    borderRadius: 999,
    background: "#ef4444",
    color: "#fff",
    fontSize: 12,
    fontWeight: 700,
    display: "grid",
    placeItems: "center",
    border: "2px solid #fff",
  },
};

export default function FloatingButton() {
  const { t } = useTranslation();
  const { openWidget, unreadCount } = useAiWidget();

  return (
    <button
      type="button"
      aria-label={t("aiWidget.open")}
      title="AI HiveMind"
      style={styles.button}
      onClick={openWidget}
      onMouseEnter={(event) => {
        event.currentTarget.style.transform = "translateY(-2px) scale(1.03)";
        event.currentTarget.style.boxShadow = "0 18px 42px rgba(249, 115, 22, 0.44)";
      }}
      onMouseLeave={(event) => {
        event.currentTarget.style.transform = "none";
        event.currentTarget.style.boxShadow = styles.button.boxShadow;
      }}
    >
      <span style={styles.icon}>AI</span>
      {unreadCount > 0 && (
        <span style={styles.badge}>{unreadCount > 9 ? "9+" : unreadCount}</span>
      )}
    </button>
  );
}
