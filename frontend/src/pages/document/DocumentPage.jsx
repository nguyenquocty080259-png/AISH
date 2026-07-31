import { useTranslation } from "react-i18next";
import { useDocumentPage } from "./hooks/useDocumentPage";
import SearchBar from "./components/SearchBar";
import FilterBar from "./components/FilterBar";
import DocumentCard from "./components/DocumentCard";
import Pagination from "./components/Pagination";
import UploadModal from "./components/UploadModal";
import StorageUsageBar from "../../components/ui/StorageUsageBar";
import PageHeader from "../../components/ui/PageHeader";
import Button from "../../components/ui/Button";
import EmptyState from "../../components/ui/EmptyState";
import { SkeletonCard } from "../../components/ui/Skeleton";
import "./document.css";

const IconUpload = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
    <path d="m17 8-5-5-5 5" />
    <path d="M12 3v12" />
  </svg>
);

export default function DocumentPage() {
  const { t } = useTranslation();
  const {
    loading,
    documents,
    subjects,
    searchText,
    setSearchText,
    subjectFilter,
    setSubjectFilter,
    resetFilters,
    page,
    totalPages,
    setPage,
    isUploadOpen,
    setIsUploadOpen,
    uploading,
    storageUsage,
    allowedFileTypes,
    handleUpload,
    handleToggleFavorite,
  } = useDocumentPage();

  const filtering = searchText.trim() !== "" || subjectFilter !== "";

  return (
    <div className="page-shell doc-page">
      <PageHeader
        title={t("documents.title")}
        subtitle={t("documents.subtitle")}
        actions={
          <Button leftIcon={<IconUpload />} onClick={() => setIsUploadOpen(true)}>
            {t("documents.upload")}
          </Button>
        }
      />

      <div className="doc-page__bar">
        <div className="doc-page__toolbar">
          <SearchBar value={searchText} onChange={setSearchText} />
          <FilterBar
            subjects={subjects}
            subjectFilter={subjectFilter}
            onSubjectChange={setSubjectFilter}
            onReset={resetFilters}
          />
        </div>

        <StorageUsageBar usage={storageUsage} className="doc-page__storage" />
      </div>

      {loading ? (
        <div className="doc-grid" aria-busy="true" aria-label={t("documents.loading")}>
          {Array.from({ length: 8 }).map((_, i) => <SkeletonCard key={i} />)}
        </div>
      ) : documents.length === 0 ? (
        <EmptyState
          icon={filtering ? "🔍" : "📄"}
          title={t("documents.empty")}
          message={filtering ? t("documents.emptyFilterHint") : t("documents.emptyHint")}
          actionLabel={filtering ? t("documents.resetFilter") : t("documents.upload")}
          onAction={filtering ? resetFilters : () => setIsUploadOpen(true)}
        />
      ) : (
        <div className="doc-grid">
          {documents.map((doc) => (
            <DocumentCard
              key={doc.id}
              doc={doc}
              onToggleFavorite={handleToggleFavorite}
            />
          ))}
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      <UploadModal
        open={isUploadOpen}
        subjects={subjects}
        submitting={uploading}
        storageUsage={storageUsage}
        allowedFileTypes={allowedFileTypes}
        onClose={() => setIsUploadOpen(false)}
        onSubmit={handleUpload}
      />
    </div>
  );
}
