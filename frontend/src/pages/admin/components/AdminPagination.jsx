import { useTranslation } from "react-i18next";

// Giữ NGUYÊN props: page, totalPages, onPrev, onNext.
export default function AdminPagination({ page, totalPages, onPrev, onNext }) {
  const { t } = useTranslation();
  if (totalPages <= 1) return null;
  return (
    <nav className="mt-6 flex items-center justify-center gap-4 text-sm" aria-label={t("admin.common.paginationAria")}>
      <button type="button" disabled={page <= 0} onClick={onPrev}
        className="rounded-input border border-border px-4 py-2 text-secondary transition-colors hover:border-primary hover:text-primary disabled:opacity-50">
        {t("admin.common.prev")}
      </button>
      <span className="text-secondary">{t("admin.common.pageOf", { page: page + 1, total: totalPages })}</span>
      <button type="button" disabled={page >= totalPages - 1} onClick={onNext}
        className="rounded-input border border-border px-4 py-2 text-secondary transition-colors hover:border-primary hover:text-primary disabled:opacity-50">
        {t("admin.common.next")}
      </button>
    </nav>
  );
}
