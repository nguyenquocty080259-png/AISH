import { useTranslation } from "react-i18next";

export default function Pagination({ page, totalPages, onPageChange }) {
  const { t } = useTranslation();
  if (totalPages <= 1) return null;

  return (
    <div className="doc-pagination">
      <button
        type="button"
        disabled={page <= 1}
        onClick={() => onPageChange(page - 1)}
      >
        {t("documents.prev")}
      </button>
      <span>
        {t("documents.pageOf", { page, total: totalPages })}
      </span>
      <button
        type="button"
        disabled={page >= totalPages}
        onClick={() => onPageChange(page + 1)}
      >
        {t("documents.next")}
      </button>
    </div>
  );
}
