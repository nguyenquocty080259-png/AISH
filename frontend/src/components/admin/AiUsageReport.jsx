import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { getAiUsageReport } from "../../api/adminApi";
import { useToast } from "../../hooks/useToast";
import "./ai-usage-report.css";

const GRANULARITIES = ["DAY", "WEEK", "MONTH", "QUARTER", "YEAR"];

function currentYearRange() {
  const year = new Date().getFullYear();
  return { from: `${year}-01-01`, to: `${year}-12-31` };
}

// "to" người dùng chọn là bao gồm cả ngày đó, nhưng BE lọc created_at < to (exclusive) - nên
// phải gửi lên "to" + 1 ngày để không bị cắt mất dữ liệu của chính ngày kết thúc.
function addOneDayIso(dateStr) {
  const [year, month, day] = dateStr.split("-").map(Number);
  const date = new Date(Date.UTC(year, month - 1, day + 1));
  const yyyy = date.getUTCFullYear();
  const mm = String(date.getUTCMonth() + 1).padStart(2, "0");
  const dd = String(date.getUTCDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

function formatBucketLabel(bucketIso, granularity, locale) {
  const date = new Date(bucketIso);
  if (Number.isNaN(date.getTime())) return bucketIso;

  switch (granularity) {
    case "DAY":
    case "WEEK":
      return date.toLocaleDateString(locale);
    case "MONTH":
      return date.toLocaleDateString(locale, { year: "numeric", month: "short" });
    case "QUARTER":
      return `Q${Math.floor(date.getMonth() / 3) + 1} ${date.getFullYear()}`;
    case "YEAR":
      return String(date.getFullYear());
    default:
      return bucketIso;
  }
}

export default function AiUsageReport() {
  const { t, i18n } = useTranslation();
  const { showError } = useToast();
  const locale = i18n.resolvedLanguage === "en" ? "en-US" : "vi-VN";

  const defaultRange = currentYearRange();
  const [granularity, setGranularity] = useState("MONTH");
  const [from, setFrom] = useState(defaultRange.from);
  const [to, setTo] = useState(defaultRange.to);
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (from === "" || to === "") return;

    setLoading(true);
    setError(false);
    getAiUsageReport(granularity, from, addOneDayIso(to))
      .then((data) => setReport(data))
      .catch((err) => {
        setError(true);
        showError(err.message);
      })
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [granularity, from, to]);

  const buckets = report?.buckets || [];
  const maxTokens = buckets.reduce((max, bucket) => Math.max(max, bucket.tokens), 0);

  return (
    <section className="ai-usage-report">
      <h2 className="ai-usage-report__title">{t("admin.aiReport.title")}</h2>

      <div className="ai-usage-report__controls">
        <label className="ai-usage-report__field">
          {t("admin.aiReport.granularityLabel")}
          <select value={granularity} onChange={(e) => setGranularity(e.target.value)}>
            {GRANULARITIES.map((g) => (
              <option key={g} value={g}>
                {t(`admin.aiReport.granularity${g.charAt(0)}${g.slice(1).toLowerCase()}`)}
              </option>
            ))}
          </select>
        </label>

        <label className="ai-usage-report__field">
          {t("admin.aiReport.fromLabel")}
          <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        </label>

        <label className="ai-usage-report__field">
          {t("admin.aiReport.toLabel")}
          <input type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        </label>
      </div>

      {loading && <p className="ai-usage-report__status">{t("admin.aiReport.loading")}</p>}
      {!loading && error && <p className="ai-usage-report__status">{t("admin.aiReport.loadError")}</p>}

      {!loading && !error && report && (
        <>
          <div className="ai-usage-report__summary">
            <div className="ai-usage-report__summary-item">
              <strong>{report.totalTokens.toLocaleString()}</strong>
              <span>{t("admin.aiReport.totalTokens")}</span>
            </div>
            <div className="ai-usage-report__summary-item">
              <strong>{report.totalCalls.toLocaleString()}</strong>
              <span>{t("admin.aiReport.totalCalls")}</span>
            </div>
          </div>

          {buckets.length === 0 ? (
            <p className="ai-usage-report__status">{t("admin.aiReport.empty")}</p>
          ) : (
            <>
              <div className="ai-usage-report__chart">
                {buckets.map((bucket) => (
                  <div className="ai-usage-report__bar-row" key={bucket.bucket}>
                    <span className="ai-usage-report__bar-label">
                      {formatBucketLabel(bucket.bucket, granularity, locale)}
                    </span>
                    <div className="ai-usage-report__bar-track">
                      <div
                        className="ai-usage-report__bar-fill"
                        style={{ width: `${maxTokens > 0 ? (bucket.tokens / maxTokens) * 100 : 0}%` }}
                      />
                    </div>
                    <span className="ai-usage-report__bar-value">{bucket.tokens.toLocaleString()}</span>
                  </div>
                ))}
              </div>

              <table className="ai-usage-report__table">
                <thead>
                  <tr>
                    <th>{t("admin.aiReport.colBucket")}</th>
                    <th>{t("admin.aiReport.colTokens")}</th>
                    <th>{t("admin.aiReport.colCalls")}</th>
                  </tr>
                </thead>
                <tbody>
                  {buckets.map((bucket) => (
                    <tr key={bucket.bucket}>
                      <td>{formatBucketLabel(bucket.bucket, granularity, locale)}</td>
                      <td>{bucket.tokens.toLocaleString()}</td>
                      <td>{bucket.calls.toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </>
          )}
        </>
      )}
    </section>
  );
}
