import { useDocumentPage } from "./hooks/useDocumentPage";
import SearchBar from "./components/SearchBar";
import FilterBar from "./components/FilterBar";
import DocumentCard from "./components/DocumentCard";
import Pagination from "./components/Pagination";
import UploadModal from "./components/UploadModal";
import "./document.css";

export default function DocumentPage() {
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
    handleUpload,
    handleToggleFavorite,
  } = useDocumentPage();

  return (
    <div className="doc-page">
      <div className="doc-page__header">
        <h1 className="doc-page__title">Tài liệu</h1>
        <button className="doc-page__upload-btn" onClick={() => setIsUploadOpen(true)}>
          + Tải lên tài liệu
        </button>
      </div>

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
        <p className="doc-empty">Đang tải danh sách tài liệu...</p>
      ) : documents.length === 0 ? (
        <p className="doc-empty">Không tìm thấy tài liệu phù hợp.</p>
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
        onClose={() => setIsUploadOpen(false)}
        onSubmit={handleUpload}
      />
    </div>
  );
}