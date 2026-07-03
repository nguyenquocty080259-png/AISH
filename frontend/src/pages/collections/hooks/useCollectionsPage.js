import { useEffect, useState } from "react";
import * as collectionApi from "../../../api/collectionApi";
import { useToast } from "../../../hooks/useToast";

export function useCollectionsPage() {
  const { showSuccess, showError } = useToast();

  const [collections, setCollections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [creating, setCreating] = useState(false);

  const [renameTarget, setRenameTarget] = useState(null);
  const [renaming, setRenaming] = useState(false);

  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await collectionApi.listMyCollections();
      setCollections(data);
    } catch (err) {
      setError(err.message);
      showError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const openCreateModal = () => setCreateModalOpen(true);
  const closeCreateModal = () => setCreateModalOpen(false);

  const handleCreate = async (name) => {
    setCreating(true);
    try {
      const created = await collectionApi.createCollection(name);
      setCollections((prev) => [created, ...prev]);
      showSuccess("Đã tạo collection mới.");
      setCreateModalOpen(false);
    } catch (err) {
      showError(err.message);
    } finally {
      setCreating(false);
    }
  };

  const openRenameModal = (collection) => setRenameTarget(collection);
  const closeRenameModal = () => setRenameTarget(null);

  const handleRename = async (name) => {
    if (!renameTarget) return;
    setRenaming(true);
    try {
      const updated = await collectionApi.renameCollection(renameTarget.id, name);
      setCollections((prev) => prev.map((c) => (c.id === updated.id ? updated : c)));
      showSuccess("Đã đổi tên collection.");
      setRenameTarget(null);
    } catch (err) {
      showError(err.message);
    } finally {
      setRenaming(false);
    }
  };

  const openDeleteModal = (collection) => setDeleteTarget(collection);
  const closeDeleteModal = () => setDeleteTarget(null);

  const handleDeleteConfirm = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await collectionApi.deleteCollection(deleteTarget.id);
      setCollections((prev) => prev.filter((c) => c.id !== deleteTarget.id));
      showSuccess("Đã xóa collection.");
      setDeleteTarget(null);
    } catch (err) {
      showError(err.message);
    } finally {
      setDeleting(false);
    }
  };

  return {
    collections,
    loading,
    error,

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
  };
}
