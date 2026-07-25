import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useCommunityPage } from "./useCommunityPage";
import { ROUTES, buildRoute } from "../../constants/routes";
import DocumentThumb from "../../components/ui/DocumentThumb";

export default function CommunityPage() {
  const { t } = useTranslation();
  const {
    items, loading, keyword, setKeyword, sortBy, setSortBy,
    subjectId, setSubjectId, minRating, setMinRating, subjects,
    resetFilters, page, setPage, totalPages,
  } = useCommunityPage();

  const selectCls = "rounded-pill border border-border bg-surface px-4 py-2.5 text-sm text-app outline-none focus:border-primary cursor-pointer";

  return (
    <div className="px-6 lg:px-8 py-6 text-app">
      <h1 className="mb-6 text-2xl font-bold">{t("community.title")}</h1>

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <div className="relative min-w-[220px] flex-1">
          <svg className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-secondary" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="11" cy="11" r="8" /><path d="m21 21-4.3-4.3" /></svg>
          <input type="text" value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder={t("community.searchPlaceholder")}
            className="w-full rounded-pill border border-border bg-surface py-2.5 pl-11 pr-4 text-sm text-app outline-none focus:border-primary" />
        </div>
        <select value={subjectId} onChange={(e) => setSubjectId(e.target.value)} className={selectCls}>
          <option value="">{t("community.allSubjects")}</option>
          {subjects.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
        </select>
        <select value={minRating} onChange={(e) => setMinRating(e.target.value)} className={selectCls}>
          <option value="">{t("community.anyRating")}</option>
          <option value="4">{t("community.rating4")}</option>
          <option value="3">{t("community.rating3")}</option>
          <option value="2">{t("community.rating2")}</option>
          <option value="1">{t("community.rating1")}</option>
        </select>
        <select value={sortBy} onChange={(e) => setSortBy(e.target.value)} className={selectCls}>
          <option value="newest">{t("community.sortNewest")}</option>
          <option value="downloads">{t("community.sortDownloads")}</option>
          <option value="rating">{t("community.sortRating")}</option>
        </select>
        <button type="button" onClick={resetFilters}
          className="rounded-pill border border-border bg-surface px-4 py-2.5 text-sm font-medium text-secondary transition-colors hover:border-primary hover:text-primary">
          {t("community.resetFilter")}
        </button>
      </div>

      {loading ? (
        <p className="py-16 text-center text-secondary">{t("community.loading")}</p>
      ) : items.length === 0 ? (
        <p className="py-16 text-center text-secondary">{t("community.empty")}</p>
      ) : (
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {items.map((doc) => {
            const detail = buildRoute(ROUTES.DOCUMENT_DETAIL, { id: doc.id });
            return (
              <article key={doc.id} className="flex flex-col rounded-card border border-border bg-surface shadow-sm transition-transform hover:-translate-y-1">
                <Link to={detail} className="doc-card__thumb-link block p-3 pb-0">
                  <DocumentThumb doc={doc} />
                </Link>
                <div className="flex flex-1 flex-col p-4 pt-3">
                  {doc.subjectNames?.length > 0 && (
                    <span className="mb-1 text-xs font-semibold uppercase tracking-wide text-primary">{doc.subjectNames.join(", ")}</span>
                  )}
                  <Link to={detail} className="line-clamp-2 font-semibold text-app hover:text-primary">{doc.title}</Link>
                  <p className="mt-1 line-clamp-2 flex-1 text-sm text-secondary">{doc.description}</p>
                  <div className="mt-3 flex items-center justify-between border-t border-border pt-3 text-xs text-secondary">
                    <span className="truncate">{doc.ownerName}</span>
                    <span className="flex items-center gap-3">
                      <span className="text-primary-dark">★ {doc.averageRating?.toFixed?.(1) ?? "—"}</span>
                      <span>⬇ {doc.downloadCount ?? 0}</span>
                    </span>
                  </div>
                </div>
              </article>
            );
          })}
        </div>
      )}

      {totalPages > 1 && (
        <div className="mt-8 flex items-center justify-center gap-4 text-sm">
          <button disabled={page <= 0} onClick={() => setPage((p) => p - 1)}
            className="rounded-input border border-border px-4 py-2 text-secondary transition-colors hover:border-primary hover:text-primary disabled:opacity-50">{t("community.prev")}</button>
          <span className="text-secondary">{t("community.pageOf", { page: page + 1, total: totalPages })}</span>
          <button disabled={page >= totalPages - 1} onClick={() => setPage((p) => p + 1)}
            className="rounded-input border border-border px-4 py-2 text-secondary transition-colors hover:border-primary hover:text-primary disabled:opacity-50">{t("community.next")}</button>
        </div>
      )}
    </div>
  );
}
