import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import PageHeader from "../../components/ui/PageHeader";
import Button from "../../components/ui/Button";
import Modal from "../../components/ui/Modal";
import Badge from "../../components/ui/Badge";
import EmptyState from "../../components/ui/EmptyState";
import CollectionDocTile from "./components/CollectionDocTile";
import { useCollectionDetailPage } from "./hooks/useCollectionDetailPage";
import { ROUTES } from "../../constants/routes";
import "./collection-detail.css";

export default function CollectionDetailPage() {
  const {
    collection,
    items,
    loading,
    removeDocument,
    removingDocId,
    addDocsModalOpen,
    availableDocs,
    filteredAvailableDocs,
    loadingAvailableDocs,
    selectedDocIds,
    addingDocs,
    openAddDocsModal,
    closeAddDocsModal,
    toggleSelectDoc,
    handleAddDocuments,
    docQuery,
    setDocQuery,
    storageFilter,
    setStorageFilter,
    subjectFilter,
    setSubjectFilter,
    subjectOptions,
    renameModalOpen,
    renaming,
    openRenameModal,
    closeRenameModal,
    renameCollection,
    deleteModalOpen,
    deleting,
    openDeleteModal,
    closeDeleteModal,
    deleteCollection,
  } = useCollectionDetailPage();

  const [renameName, setRenameName] = useState("");

  useEffect(() => {
    if (renameModalOpen) setRenameName(collection?.name ?? "");
  }, [renameModalOpen, collection]);

  const submitRename = (e) => {
    e.preventDefault();
    if (!renameName.trim()) return;
    renameCollection(renameName.trim());
  };

  if (loading) {
    return <div className="collection-detail-page">Đang tải collection...</div>;
  }

  if (!collection) {
    return <div className="collection-detail-page">Không tìm thấy collection.</div>;
  }

  return (
    <div className="collection-detail-page">
      <Link to={ROUTES.SPACES} className="collection-detail-page__back">
        ‹ Quay lại Spaces
      </Link>

      <PageHeader
        title={collection.name}
        subtitle={`${items.length} tài liệu`}
        actions={
          <>
            <Button variant="primary" onClick={openAddDocsModal}>
              + Thêm tài liệu
            </Button>
            <Button variant="secondary" onClick={openRenameModal}>
              Đổi tên
            </Button>
            <Button variant="danger" onClick={openDeleteModal}>
              Xóa collection
            </Button>
          </>
        }
      />

      {items.length === 0 ? (
        <EmptyState
          icon="🗂️"
          message="Chưa có tài liệu nào trong collection này."
          actionLabel="Thêm tài liệu"
          onAction={openAddDocsModal}
        />
      ) : (
        <div className="collection-detail-grid">
          {items.map((item) => (
            <CollectionDocTile
              key={item.documentId}
              item={item}
              onRemove={removeDocument}
              removing={removingDocId === item.documentId}
            />
          ))}
        </div>
      )}

      <Modal open={renameModalOpen} onClose={closeRenameModal} title="Đổi tên collection">
        <form className="collection-detail-form" onSubmit={submitRename}>
          <label className="collection-detail-form__field">
            Tên collection
            <input
              type="text"
              autoFocus
              value={renameName}
              onChange={(e) => setRenameName(e.target.value)}
            />
          </label>
          <div className="collection-detail-form__actions">
            <Button type="button" variant="secondary" onClick={closeRenameModal} disabled={renaming}>
              Hủy
            </Button>
            <Button type="submit" variant="primary" disabled={renaming}>
              {renaming ? "Đang lưu..." : "Lưu"}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal open={deleteModalOpen} onClose={closeDeleteModal} title="Xóa collection">
        <p className="collection-detail-confirm__text">
          Xóa collection "{collection.name}"? Tài liệu bên trong sẽ không bị xóa.
        </p>
        <div className="collection-detail-form__actions">
          <Button variant="secondary" onClick={closeDeleteModal} disabled={deleting}>
            Hủy
          </Button>
          <Button variant="danger" onClick={deleteCollection} disabled={deleting}>
            {deleting ? "Đang xóa..." : "Xóa"}
          </Button>
        </div>
      </Modal>

      <Modal open={addDocsModalOpen} onClose={closeAddDocsModal} title="Thêm tài liệu vào collection">
        {loadingAvailableDocs ? (
          <p className="collection-detail-add__loading">Đang tải danh sách tài liệu...</p>
        ) : availableDocs.length === 0 ? (
          <p className="collection-detail-add__loading">
            Không còn tài liệu nào khác để thêm vào collection này.
          </p>
        ) : (
          <>
            <div className="collection-detail-add__filters">
              <input
                type="text"
                className="collection-detail-add__search"
                placeholder="Tìm tài liệu theo tên..."
                value={docQuery}
                onChange={(e) => setDocQuery(e.target.value)}
              />
              <div className="collection-detail-add__filter-row">
                <div className="collection-detail-add__segmented">
                  <button
                    type="button"
                    className={storageFilter === "ALL" ? "is-active" : ""}
                    onClick={() => setStorageFilter("ALL")}
                  >
                    Tất cả
                  </button>
                  <button
                    type="button"
                    className={storageFilter === "LOCAL" ? "is-active" : ""}
                    onClick={() => setStorageFilter("LOCAL")}
                  >
                    LOCAL
                  </button>
                  <button
                    type="button"
                    className={storageFilter === "CLOUD" ? "is-active" : ""}
                    onClick={() => setStorageFilter("CLOUD")}
                  >
                    CLOUD
                  </button>
                </div>
                <select
                  className="collection-detail-add__subject-select"
                  value={subjectFilter}
                  onChange={(e) =>
                    setSubjectFilter(e.target.value === "ALL" ? "ALL" : Number(e.target.value))
                  }
                >
                  <option value="ALL">Tất cả môn</option>
                  {subjectOptions.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {filteredAvailableDocs.length === 0 ? (
              <p className="collection-detail-add__loading">Không tìm thấy tài liệu phù hợp.</p>
            ) : (
              <ul className="collection-detail-add__list">
                {filteredAvailableDocs.map((doc) => (
                  <li key={doc.id} className="collection-detail-add__item">
                    <label>
                      <input
                        type="checkbox"
                        checked={selectedDocIds.includes(doc.id)}
                        onChange={() => toggleSelectDoc(doc.id)}
                      />
                      {doc.title}
                    </label>
                    {doc.storageType && <Badge intent="info">{doc.storageType}</Badge>}
                  </li>
                ))}
              </ul>
            )}
          </>
        )}

        <p className="collection-detail-add__note">
          Thêm vào collection chỉ là tham chiếu — tài liệu vẫn còn nguyên trong My Documents.
        </p>

        <div className="collection-detail-form__actions">
          <Button variant="secondary" onClick={closeAddDocsModal} disabled={addingDocs}>
            Hủy
          </Button>
          <Button
            variant="primary"
            onClick={handleAddDocuments}
            disabled={addingDocs || selectedDocIds.length === 0}
          >
            {addingDocs ? "Đang thêm..." : `Thêm (${selectedDocIds.length})`}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
