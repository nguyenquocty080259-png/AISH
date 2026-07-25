import { useTranslation } from "react-i18next";
import { useDocumentPage } from "./hooks/useDocumentPage";
import SearchBar from "./components/SearchBar";
import FilterBar from "./components/FilterBar";
import DocumentCard from "./components/DocumentCard";
import Pagination from "./components/Pagination";
import UploadModal from "./components/UploadModal";
import StorageUsageBar from "../../components/ui/StorageUsageBar";
import "./document.css";

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

  return (
    <div className="doc-page">
      <div className="doc-page__header">
        <h1 className="doc-page__title">{t("documents.title")}</h1>
        <button className="doc-page__upload-btn" onClick={() => setIsUploadOpen(true)}>
          {t("documents.upload")}
        </button>
      </div>

      <StorageUsageBar usage={storageUsage} className="doc-page__storage" />

      <div className="doc-page__toolbar">
        <SearchBar value={searchText} onChange={setSearchText} />
        <FilterBar
          subjects={subjects}
          subjectFilter={subjectFilter}
          onSubjectChange={setSubjectFilter}
          onReset={resetFilters}
        />
      </div>

      {loading ? (
        <p className="doc-empty">{t("documents.loading")}</p>
      ) : documents.length === 0 ? (
        <p className="doc-empty">{t("documents.empty")}</p>
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