import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
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

function Period({ label, value, t }) {
  return (
    <div style={{ padding: "12px", borderRadius: "10px", background: "#dbeafe" }}>
      <strong style={{ color: "#1d4ed8" }}>{label}</strong>
      <div style={metricGrid}>
        <span><b>{value.totalCalls.toLocaleString()}</b><br />{t("admin.aiUsage.calls")}</span>
        <span><b>{value.totalTokens.toLocaleString()}</b><br />{t("admin.aiUsage.tokens")}</span>
      </div>
    </div>
  );
}

export default function AdminAiUsageCard() {
  const { t } = useTranslation();
  const [usage, setUsage] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    getAiUsage().then(setUsage).catch(() => setError(true));
  }, []);

  return (
    <section style={panel} aria-label={t("admin.aiUsage.aria")}>
      <h2 style={{ margin: "0 0 12px", color: "#1e3a8a", fontSize: "1.1rem" }}>{t("admin.aiUsage.title")}</h2>
      {error && <p>{t("admin.aiUsage.loadError")}</p>}
      {!error && !usage && <p>{t("admin.aiUsage.loading")}</p>}
      {usage && (
        <>
          <div style={metricGrid}>
            <Period label={t("admin.aiUsage.today")} value={usage.today} t={t} />
            <Period label={t("admin.aiUsage.last7Days")} value={usage.last7Days} t={t} />
          </div>
          <div style={{ marginTop: "12px", color: "#334155", fontSize: "0.88rem" }}>
            {(usage.last7Days.byCallType || []).map((item) => (
              <div key={item.callType}>
                <b>{item.callType}</b>: {item.totalCalls.toLocaleString()} calls · {item.totalTokens.toLocaleString()} tokens
              </div>
            ))}
            <small style={{ display: "block", marginTop: "6px", color: "#64748b", fontSize: "0.76rem" }}>
              {t("admin.aiUsage.footnote")}
            </small>
          </div>
        </>
      )}
    </section>
  );
}
