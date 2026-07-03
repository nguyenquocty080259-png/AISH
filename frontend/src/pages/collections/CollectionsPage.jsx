import { useEffect, useState } from "react";
import PageHeader from "../../components/ui/PageHeader";
import EmptyState from "../../components/ui/EmptyState";
import Modal from "../../components/ui/Modal";
import Button from "../../components/ui/Button";
import CollectionCard from "./components/CollectionCard";
import { useCollectionsPage } from "./hooks/useCollectionsPage";
import "./collections.css";

export default function CollectionsPage() {
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
        title="Spaces"
        actions={
          <Button variant="primary" onClick={openCreateModal}>
            + Tạo collection mới
          </Button>
        }
      />

      {loading ? (
        <p className="collections-page__loading">Đang tải...</p>
      ) : collections.length === 0 ? (
        <EmptyState
          icon="🗂️"
          message="Chưa có collection nào."
          actionLabel="Tạo collection mới"
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

      <Modal open={createModalOpen} onClose={closeCreateModal} title="Tạo collection mới">
        <form className="collections-form" onSubmit={submitCreate}>
          <label className="collections-form__field">
            Tên collection
            <input
              type="text"
              autoFocus
              value={createName}
              onChange={(e) => setCreateName(e.target.value)}
            />
          </label>
          <div className="collections-form__actions">
            <Button type="button" variant="secondary" onClick={closeCreateModal} disabled={creating}>
              Hủy
            </Button>
            <Button type="submit" variant="primary" disabled={creating}>
              {creating ? "Đang tạo..." : "Tạo"}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal open={!!renameTarget} onClose={closeRenameModal} title="Đổi tên collection">
        <form className="collections-form" onSubmit={submitRename}>
          <label className="collections-form__field">
            Tên collection
            <input
              type="text"
              autoFocus
              value={renameName}
              onChange={(e) => setRenameName(e.target.value)}
            />
          </label>
          <div className="collections-form__actions">
            <Button type="button" variant="secondary" onClick={closeRenameModal} disabled={renaming}>
              Hủy
            </Button>
            <Button type="submit" variant="primary" disabled={renaming}>
              {renaming ? "Đang lưu..." : "Lưu"}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal open={!!deleteTarget} onClose={closeDeleteModal} title="Xóa collection">
        <p className="collections-confirm__text">
          Xóa collection "{deleteTarget?.name}"? Tài liệu bên trong sẽ không bị xóa.
        </p>
        <div className="collections-form__actions">
          <Button variant="secondary" onClick={closeDeleteModal} disabled={deleting}>
            Hủy
          </Button>
          <Button variant="danger" onClick={handleDeleteConfirm} disabled={deleting}>
            {deleting ? "Đang xóa..." : "Xóa"}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
