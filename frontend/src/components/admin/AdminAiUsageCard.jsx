import { useEffect, useState } from "react";
import { getAiUsage } from "../../api/adminApi";

const panel = {
  padding: "18px",
  border: "1px solid #bfdbfe",
  borderRadius: "14px",
  background: "linear-gradient(135deg, #eff6ff 0%, #ffffff 100%)",
  boxShadow: "0 8px 24px rgba(37, 99, 235, 0.08)",
};

const metricGrid = {
  display: "grid",
  gridTemplateColumns: "repeat(auto-fit, minmax(130px, 1fr))",
  gap: "10px",
};

function Period({ label, value }) {
  return (
    <div style={{ padding: "12px", borderRadius: "10px", background: "#dbeafe" }}>
      <strong style={{ color: "#1d4ed8" }}>{label}</strong>
      <div style={metricGrid}>
        <span><b>{value.totalCalls.toLocaleString()}</b><br />lượt gọi</span>
        <span><b>{value.totalTokens.toLocaleString()}</b><br />tokens</span>
        <span><b>${value.totalCostUsd.toFixed(6)}</b><br />chi phí</span>
      </div>
    </div>
  );
}

export default function AdminAiUsageCard() {
  const [usage, setUsage] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    getAiUsage().then(setUsage).catch(() => setError(true));
  }, []);

  return (
    <section style={panel} aria-label="Thống kê sử dụng AI">
      <h2 style={{ margin: "0 0 12px", color: "#1e3a8a", fontSize: "1.1rem" }}>Sử dụng AI & chi phí Groq</h2>
      {error && <p>Không tải được thống kê sử dụng AI.</p>}
      {!error && !usage && <p>Đang tải thống kê AI...</p>}
      {usage && (
        <>
          <div style={metricGrid}>
            <Period label="Hôm nay" value={usage.today} />
            <Period label="7 ngày gần nhất" value={usage.last7Days} />
          </div>
          <div style={{ marginTop: "12px", color: "#334155", fontSize: "0.88rem" }}>
            {(usage.last7Days.byCallType || []).map((item) => (
              <div key={item.callType}>
                <b>{item.callType}</b>: {item.totalCalls.toLocaleString()} calls · {item.totalTokens.toLocaleString()} tokens · ${item.totalCostUsd.toFixed(6)}
              </div>
            ))}
          </div>
        </>
      )}
    </section>
  );
}
