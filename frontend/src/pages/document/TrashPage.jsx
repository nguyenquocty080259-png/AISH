import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useTrashPage } from "./hooks/useTrashPage";
import { ROUTES } from "../../constants/routes";
import PageHeader from "../../components/ui/PageHeader";
import Button from "../../components/ui/Button";
import Badge from "../../components/ui/Badge";
import Alert from "../../components/ui/Alert";
import EmptyState from "../../components/ui/EmptyState";
import { SkeletonCard } from "../../components/ui/Skeleton";
import "./document.css";

const DAYS_TO_DELETE = 30;

function daysLeft(deletedAt) {
  if (!deletedAt) return null;
  const deleted = new Date(deletedAt);
  const expire = new Date(deleted.getTime() + DAYS_TO_DELETE * 86400000);
  const left = Math.ceil((expire - Date.now()) / 86400000);
  return left;
}

// Trang THÙNG RÁC: liệt kê tài liệu đã xoá mềm, hiện số ngày còn lại trước khi tự động xoá vĩnh
// viễn, cho phép khôi phục hoặc xoá vĩnh viễn ngay.
// Nút "Quay lại" là điều hướng nên render bằng <Link>, chỉ mượn class của Button
// để trông giống nút secondary mà vẫn đúng ngữ nghĩa thẻ <a>.
export default function TrashPage() {
  const { t } = useTranslation();
  const { documents, loading, handleRestore, handlePermanentDelete } = useTrashPage();

  return (
    <div className="page-shell doc-page">
      <PageHeader
        title={t("trash.title")}
        actions={
          <Link
            to={ROUTES.DOCUMENTS}
            className="ui-btn ui-btn--secondary ui-btn--md has-custom-focus"
          >
            {t("trash.back")}
          </Link>
        }
      />

      <Alert intent="neutral" className="trash-page__note">
        {t("trash.autoDeleteNote", { days: DAYS_TO_DELETE })}
      </Alert>

      {loading ? (
        <div className="doc-grid" aria-busy="true" aria-label={t("trash.loading")}>
          {Array.from({ length: 4 }).map((_, i) => <SkeletonCard key={i} />)}
        </div>
      ) : documents.length === 0 ? (
        <EmptyState icon="🗑️" title={t("trash.empty")} />
      ) : (
        <div className="doc-grid">
          {documents.map((doc) => {
            const left = daysLeft(doc.deletedAt);
            const urgent = left != null && left <= 5;
            return (
              <article key={doc.id} className="doc-tile trash-card">
                <div className="doc-tile__body">
                  <h3 className="doc-tile__title">{doc.title}</h3>
                  <p className="doc-tile__desc">{doc.fileName}</p>

                  {left != null && (
                    <div className="doc-tile__badges">
                      <Badge intent={urgent ? "danger" : "neutral"} size="sm" dot={urgent}>
                        {left > 0 ? t("trash.daysLeft", { count: left }) : t("trash.soonDelete")}
                      </Badge>
                    </div>
                  )}

                  <div className="trash-card__actions">
                    <Button size="sm" onClick={() => handleRestore(doc.id)}>
                      {t("trash.restore")}
                    </Button>
                    <Button size="sm" variant="danger" onClick={() => handlePermanentDelete(doc.id)}>
                      {t("trash.permanentDelete")}
                    </Button>
                  </div>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}
