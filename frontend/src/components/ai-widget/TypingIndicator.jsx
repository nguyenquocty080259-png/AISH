const styles = {
  row: {
    display: "flex",
    justifyContent: "flex-start",
    margin: "8px 0",
  },
  bubble: {
    padding: "9px 12px",
    borderRadius: "16px 16px 16px 4px",
    background: "#fff",
    color: "#6b7280",
    boxShadow: "0 4px 14px rgba(15, 23, 42, 0.08)",
    border: "1px solid rgba(15, 23, 42, 0.06)",
    fontSize: 13,
  },
  dots: {
    display: "inline-block",
    width: 22,
    marginLeft: 2,
  },
};

export default function TypingIndicator() {
  const [dots, setDots] = useState(".");

  useEffect(() => {
    const id = window.setInterval(() => {
      setDots((current) => (current.length >= 3 ? "." : `${current}.`));
    }, 350);
    return () => window.clearInterval(id);
  }, []);

  return (
    <div style={styles.row}>
      <div style={styles.bubble}>
        AI HiveMind is typing<span style={styles.dots}>{dots}</span>
      </div>
    </div>
  );
}
import { useEffect, useState } from "react";
