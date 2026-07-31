import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useCommunityPage } from "./useCommunityPage";
import { ROUTES, buildRoute } from "../../constants/routes";
import DocumentThumb from "../../components/ui/DocumentThumb";
import PageHeader from "../../components/ui/PageHeader";
import EmptyState from "../../components/ui/EmptyState";
import Pagination from "../../components/ui/Pagination";
import Button from "../../components/ui/Button";
import FormatBadge from "../../components/ui/FormatBadge";
import { SkeletonCard } from "../../components/ui/Skeleton";
import { Input, Select } from "../../components/ui/Field";
import "./community.css";

const IconSearch = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="11" cy="11" r="8" />
    <path d="m21 21-4.3-4.3" />
  </svg>
);

export default function CommunityPage() {
  const { t } = useTranslation();
  const {
    items, loading, keyword, setKeyword, sortBy, setSortBy,
    subjectId, setSubjectId, minRating, setMinRating, subjects,
    resetFilters, page, setPage, totalPages,
  } = useCommunityPage();

  return (
    <div className="page-shell community">
      <PageHeader title={t("community.title")} subtitle={t("community.subtitle")} />

      <div className="community__toolbar">
        <Input
          type="search"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder={t("community.searchPlaceholder")}
          aria-label={t("community.searchPlaceholder")}
          leftIcon={<IconSearch />}
          fieldClassName="community__search"
        />

        <Select
          value={subjectId}
          onChange={(e) => setSubjectId(e.target.value)}
          aria-label={t("community.allSubjects")}
          fieldClassName="community__filter"
        >
          <option value="">{t("community.allSubjects")}</option>
          {subjects.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
        </Select>

        <Select
          value={minRating}
          onChange={(e) => setMinRating(e.target.value)}
          aria-label={t("community.anyRating")}
          fieldClassName="community__filter"
        >
          <option value="">{t("community.anyRating")}</option>
          <option value="4">{t("community.rating4")}</option>
          <option value="3">{t("community.rating3")}</option>
          <option value="2">{t("community.rating2")}</option>
          <option value="1">{t("community.rating1")}</option>
        </Select>

        <Select
          value={sortBy}
          onChange={(e) => setSortBy(e.target.value)}
          aria-label={t("community.sortNewest")}
          fieldClassName="community__filter"
        >
          <option value="newest">{t("community.sortNewest")}</option>
          <option value="downloads">{t("community.sortDownloads")}</option>
          <option value="rating">{t("community.sortRating")}</option>
        </Select>

        <Button variant="ghost" onClick={resetFilters} className="community__reset">
          {t("community.resetFilter")}
        </Button>
      </div>

      {loading ? (
        <div className="doc-grid" aria-busy="true" aria-label={t("community.loading")}>
          {Array.from({ length: 8 }).map((_, i) => <SkeletonCard key={i} />)}
        </div>
      ) : items.length === 0 ? (
        <EmptyState
          icon="🔍"
          title={t("community.empty")}
          message={t("community.emptyHint")}
          actionLabel={t("community.resetFilter")}
          onAction={resetFilters}
        />
      ) : (
        <div className="doc-grid">
          {items.map((doc) => {
            const detail = buildRoute(ROUTES.DOCUMENT_DETAIL, { id: doc.id });
            return (
              <article key={doc.id} className="doc-tile">
                <Link to={detail} className="doc-card__thumb-link doc-tile__media" tabIndex={-1} aria-hidden="true">
                  <DocumentThumb doc={doc} />
                </Link>

                <div className="doc-tile__body">
                  {doc.subjectNames?.length > 0 && (
                    <span className="doc-tile__eyebrow">{doc.subjectNames.join(", ")}</span>
                  )}

                  <Link to={detail} className="doc-tile__title has-custom-focus">{doc.title}</Link>

                  {doc.description && <p className="doc-tile__desc">{doc.description}</p>}

                  <div className="doc-tile__badges">
                    <FormatBadge fileType={doc.fileType} fileName={doc.fileName} />
                  </div>

                  <div className="doc-tile__foot">
                    <span className="doc-tile__owner">{doc.ownerName}</span>
                    <span className="doc-tile__stats">
                      <span className="doc-tile__stat doc-tile__stat--rating">
                        <span aria-hidden="true">★</span>
                        {doc.averageRating?.toFixed?.(1) ?? "—"}
                      </span>
                      <span className="doc-tile__stat">
                        <span aria-hidden="true">⬇</span>
                        {doc.downloadCount ?? 0}
                      </span>
                    </span>
                  </div>
                </div>
              </article>
            );
          })}
        </div>
      )}

      {/* Hook đánh số trang từ 0, Pagination dùng số từ 1 -> quy đổi ở đây, không đụng hook. */}
      <Pagination
        className="community__pagination"
        page={page + 1}
        totalPages={totalPages}
        onPageChange={(next) => setPage(next - 1)}
        prevLabel={t("community.prev")}
        nextLabel={t("community.next")}
        ariaLabel={t("community.title")}
      />
    </div>
  );
}
