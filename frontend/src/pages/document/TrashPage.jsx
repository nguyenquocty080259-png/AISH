import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useTrashPage } from "./hooks/useTrashPage";
import { ROUTES } from "../../constants/routes";
import "./document.css";

const DAYS_TO_DELETE = 30;

function daysLeft(deletedAt) {
  if (!deletedAt) return null;
  const deleted = new Date(deletedAt);
  const expire = new Date(deleted.getTime() + DAYS_TO_DELETE * 86400000);
  const left = Math.ceil((expire - Date.now()) / 86400000);
  return left;
}

export default function TrashPage() {
  const { t } = useTranslation();
  const { documents, loading, handleRestore, handlePermanentDelete } = useTrashPage();

  return (
    <div className="doc-page">
      <div className="doc-page__header">
        <h1 className="doc-page__title">{t("trash.title")}</h1>
        <Link to={ROUTES.DOCUMENTS} className="doc-page__upload-btn">
          {t("trash.back")}
        </Link>
      </div>

      <p style={{ color: "#888", marginBottom: 16 }}>
        {t("trash.autoDeleteNote", { days: DAYS_TO_DELETE })}
      </p>

      {loading ? (
        <p className="doc-empty">{t("trash.loading")}</p>
      ) : documents.length === 0 ? (
        <p className="doc-empty">{t("trash.empty")}</p>
      ) : (
        <div className="doc-grid">
          {documents.map((doc) => {
            const left = daysLeft(doc.deletedAt);
            return (
              <div key={doc.id} className="doc-card">
                <h3 className="doc-card__title">{doc.title}</h3>
                <p style={{ color: "#888", fontSize: 13 }}>{doc.fileName}</p>
                {left != null && (
                  <p style={{ color: left <= 5 ? "#e11" : "#888", fontSize: 13 }}>
                    {left > 0 ? t("trash.daysLeft", { count: left }) : t("trash.soonDelete")}
                  </p>
                )}
                <div style={{ display: "flex", gap: 8, marginTop: 12 }}>
                  <button
                    className="detail-btn detail-btn--primary"
                    onClick={() => handleRestore(doc.id)}
                  >
                    {t("trash.restore")}
                  </button>
                  <button
                    className="detail-btn detail-btn--danger"
                    onClick={() => handlePermanentDelete(doc.id)}
                  >
                    {t("trash.permanentDelete")}
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
