import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import PageHeader from "../../components/ui/PageHeader";
import EmptyState from "../../components/ui/EmptyState";
import Modal from "../../components/ui/Modal";
import Button from "../../components/ui/Button";
import CollectionCard from "./components/CollectionCard";
import { useCollectionsPage } from "./hooks/useCollectionsPage";
import "./collections.css";

// Trang danh sách BỘ SƯU TẬP của tôi: lưới các bộ sưu tập, tạo mới/đổi tên/xoá qua modal.
export default function CollectionsPage() {
  const { t } = useTranslation();
  const {
    collections,
    loading,
    createModalOpen,
    creating,
    openCreateModal,
    closeCreateModal,
    handleCreate,
    renameTarget,
    renaming,
    openRenameModal,
    closeRenameModal,
    handleRename,
    deleteTarget,
    deleting,
    openDeleteModal,
    closeDeleteModal,
    handleDeleteConfirm,
  } = useCollectionsPage();

  const [createName, setCreateName] = useState("");
  const [renameName, setRenameName] = useState("");

  useEffect(() => {
    if (createModalOpen) setCreateName("");
  }, [createModalOpen]);

  useEffect(() => {
    setRenameName(renameTarget?.name ?? "");
  }, [renameTarget]);

  const submitCreate = (e) => {
    e.preventDefault();
    if (!createName.trim()) return;
    handleCreate(createName.trim());
  };

  const submitRename = (e) => {
    e.preventDefault();
    if (!renameName.trim()) return;
    handleRename(renameName.trim());
  };

  return (
    <div className="collections-page">
      <PageHeader
        title={t("collections.spacesTitle")}
        actions={
          <Button variant="primary" onClick={openCreateModal}>
            {t("collections.createNew")}
          </Button>
        }
      />

      {loading ? (
        <p className="collections-page__loading">{t("collections.loading")}</p>
      ) : collections.length === 0 ? (
        <EmptyState
          icon="🗂️"
          message={t("collections.empty")}
          actionLabel={t("collections.emptyAction")}
          onAction={openCreateModal}
        />
      ) : (
        <div className="collections-grid">
          {collections.map((c) => (
            <CollectionCard
              key={c.id}
              collection={c}
              onRename={openRenameModal}
              onDelete={openDeleteModal}
            />
          ))}
        </div>
      )}

      <Modal open={createModalOpen} onClose={closeCreateModal} title={t("collections.createTitle")}>
        <form className="collections-form" onSubmit={submitCreate}>
          <label className="collections-form__field">
            {t("collections.nameLabel")}
            <input
              type="text"
              autoFocus
              value={createName}
              onChange={(e) => setCreateName(e.target.value)}
            />
          </label>
          <div className="collections-form__actions">
            <Button type="button" variant="secondary" onClick={closeCreateModal} disabled={creating}>
              {t("common.actions.cancel")}
            </Button>
            <Button type="submit" variant="primary" disabled={creating}>
              {creating ? t("collections.creating") : t("collections.create")}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal open={!!renameTarget} onClose={closeRenameModal} title={t("collections.renameTitle")}>
        <form className="collections-form" onSubmit={submitRename}>
          <label className="collections-form__field">
            {t("collections.nameLabel")}
            <input
              type="text"
              autoFocus
              value={renameName}
              onChange={(e) => setRenameName(e.target.value)}
            />
          </label>
          <div className="collections-form__actions">
            <Button type="button" variant="secondary" onClick={closeRenameModal} disabled={renaming}>
              {t("common.actions.cancel")}
            </Button>
            <Button type="submit" variant="primary" disabled={renaming}>
              {renaming ? t("collections.saving") : t("collections.save")}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal open={!!deleteTarget} onClose={closeDeleteModal} title={t("collections.deleteTitle")}>
        <p className="collections-confirm__text">
          {t("collections.deleteConfirm", { name: deleteTarget?.name })}
        </p>
        <div className="collections-form__actions">
          <Button variant="secondary" onClick={closeDeleteModal} disabled={deleting}>
            {t("common.actions.cancel")}
          </Button>
          <Button variant="danger" onClick={handleDeleteConfirm} disabled={deleting}>
            {deleting ? t("collections.deleting") : t("collections.delete")}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
