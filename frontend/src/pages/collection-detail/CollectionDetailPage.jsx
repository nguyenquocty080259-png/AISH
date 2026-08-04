import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import PageHeader from "../../components/ui/PageHeader";
import Button from "../../components/ui/Button";
import Modal from "../../components/ui/Modal";
import Badge from "../../components/ui/Badge";
import EmptyState from "../../components/ui/EmptyState";
import CollectionDocTile from "./components/CollectionDocTile";
import { useCollectionDetailPage } from "./hooks/useCollectionDetailPage";
import { ROUTES } from "../../constants/routes";
import "./collection-detail.css";

// Trang CHI TIẾT một bộ sưu tập: lưới tài liệu bên trong, thêm tài liệu (modal có tìm kiếm/lọc
// theo nơi lưu và môn học), đổi tên, xoá bộ sưu tập, gỡ từng tài liệu khỏi bộ sưu tập.
export default function CollectionDetailPage() {
  const { t } = useTranslation();
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
    return <div className="collection-detail-page">{t("collectionDetail.loadingPage")}</div>;
  }

  if (!collection) {
    return <div className="collection-detail-page">{t("collectionDetail.notFound")}</div>;
  }

  return (
    <div className="collection-detail-page">
      <Link to={ROUTES.SPACES} className="collection-detail-page__back">
        {t("collectionDetail.back")}
      </Link>

      <PageHeader
        title={collection.name}
        subtitle={t("collectionDetail.docCount", { count: items.length })}
        actions={
          <>
            <Button variant="primary" onClick={openAddDocsModal}>
              {t("collectionDetail.addDoc")}
            </Button>
            <Button variant="secondary" onClick={openRenameModal}>
              {t("collectionDetail.rename")}
            </Button>
            <Button variant="danger" onClick={openDeleteModal}>
              {t("collectionDetail.deleteCollection")}
            </Button>
          </>
        }
      />

      {items.length === 0 ? (
        <EmptyState
          icon="🗂️"
          message={t("collectionDetail.emptyMessage")}
          actionLabel={t("collectionDetail.emptyAction")}
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

      <Modal open={renameModalOpen} onClose={closeRenameModal} title={t("collectionDetail.renameTitle")}>
        <form className="collection-detail-form" onSubmit={submitRename}>
          <label className="collection-detail-form__field">
            {t("collectionDetail.nameLabel")}
            <input
              type="text"
              autoFocus
              value={renameName}
              onChange={(e) => setRenameName(e.target.value)}
            />
          </label>
          <div className="collection-detail-form__actions">
            <Button type="button" variant="secondary" onClick={closeRenameModal} disabled={renaming}>
              {t("common.actions.cancel")}
            </Button>
            <Button type="submit" variant="primary" disabled={renaming}>
              {renaming ? t("collectionDetail.saving") : t("collectionDetail.save")}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal open={deleteModalOpen} onClose={closeDeleteModal} title={t("collectionDetail.deleteTitle")}>
        <p className="collection-detail-confirm__text">
          {t("collectionDetail.deleteConfirm", { name: collection.name })}
        </p>
        <div className="collection-detail-form__actions">
          <Button variant="secondary" onClick={closeDeleteModal} disabled={deleting}>
            {t("common.actions.cancel")}
          </Button>
          <Button variant="danger" onClick={deleteCollection} disabled={deleting}>
            {deleting ? t("collectionDetail.deleting") : t("common.actions.delete")}
          </Button>
        </div>
      </Modal>

      <Modal open={addDocsModalOpen} onClose={closeAddDocsModal} title={t("collectionDetail.addModalTitle")}>
        {loadingAvailableDocs ? (
          <p className="collection-detail-add__loading">{t("collectionDetail.addLoading")}</p>
        ) : availableDocs.length === 0 ? (
          <p className="collection-detail-add__loading">
            {t("collectionDetail.noOtherDocs")}
          </p>
        ) : (
          <>
            <div className="collection-detail-add__filters">
              <input
                type="text"
                className="collection-detail-add__search"
                placeholder={t("collectionDetail.searchByName")}
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
                    {t("collectionDetail.filterAll")}
                  </button>
                  <button
                    type="button"
                    className={storageFilter === "SERVER" ? "is-active" : ""}
                    onClick={() => setStorageFilter("SERVER")}
                  >
                    SERVER
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
                  <option value="ALL">{t("collectionDetail.filterAllSubjects")}</option>
                  {subjectOptions.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {filteredAvailableDocs.length === 0 ? (
              <p className="collection-detail-add__loading">{t("collectionDetail.noMatch")}</p>
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
          {t("collectionDetail.addNote")}
        </p>

        <div className="collection-detail-form__actions">
          <Button variant="secondary" onClick={closeAddDocsModal} disabled={addingDocs}>
            {t("common.actions.cancel")}
          </Button>
          <Button
            variant="primary"
            onClick={handleAddDocuments}
            disabled={addingDocs || selectedDocIds.length === 0}
          >
            {addingDocs ? t("collectionDetail.adding") : t("collectionDetail.addWithCount", { count: selectedDocIds.length })}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
