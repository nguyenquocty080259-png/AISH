import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import * as collectionApi from "../../../api/collectionApi";
import * as documentApi from "../../../api/documentApi";
import { useToast } from "../../../hooks/useToast";
import { ROUTES } from "../../../constants/routes";

export function useCollectionDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

  const [collection, setCollection] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [renameModalOpen, setRenameModalOpen] = useState(false);
  const [renaming, setRenaming] = useState(false);

  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const [removingDocId, setRemovingDocId] = useState(null);

  const [addDocsModalOpen, setAddDocsModalOpen] = useState(false);
  const [availableDocs, setAvailableDocs] = useState([]);
  const [loadingAvailableDocs, setLoadingAvailableDocs] = useState(false);
  const [selectedDocIds, setSelectedDocIds] = useState([]);
  const [addingDocs, setAddingDocs] = useState(false);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await collectionApi.getOne(id);
      setCollection(data);
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
  }, [id]);

  const openRenameModal = () => setRenameModalOpen(true);
  const closeRenameModal = () => setRenameModalOpen(false);

  const renameCollection = async (name) => {
    setRenaming(true);
    try {
      const updated = await collectionApi.renameCollection(id, name);
      setCollection((prev) => (prev ? { ...prev, name: updated.name } : prev));
      showSuccess("Đã đổi tên collection.");
      setRenameModalOpen(false);
    } catch (err) {
      showError(err.message);
    } finally {
      setRenaming(false);
    }
  };

  const openDeleteModal = () => setDeleteModalOpen(true);
  const closeDeleteModal = () => setDeleteModalOpen(false);

  const deleteCollection = async () => {
    setDeleting(true);
    try {
      await collectionApi.deleteCollection(id);
      showSuccess("Đã xóa collection.");
      navigate(ROUTES.SPACES);
    } catch (err) {
      showError(err.message);
      setDeleting(false);
    }
  };

  const removeDocument = async (docId) => {
    setRemovingDocId(docId);
    try {
      await collectionApi.removeDocument(id, docId);
      setCollection((prev) =>
        prev ? { ...prev, items: prev.items.filter((item) => item.documentId !== docId) } : prev
      );
      showSuccess("Đã gỡ tài liệu khỏi collection.");
    } catch (err) {
      showError(err.message);
    } finally {
      setRemovingDocId(null);
    }
  };

  // Chỉ tải "tài liệu có thể thêm" khi mở modal (không tải sẵn) — lọc bỏ những doc
  // đã có trong collection này bằng documentId đang có trong items.
  const openAddDocsModal = async () => {
    setAddDocsModalOpen(true);
    setSelectedDocIds([]);
    setLoadingAvailableDocs(true);
    try {
      const allDocs = await documentApi.getAll();
      const existingIds = new Set((collection?.items ?? []).map((item) => item.documentId));
      setAvailableDocs(allDocs.filter((d) => !existingIds.has(d.id)));
    } catch (err) {
      showError(err.message);
    } finally {
      setLoadingAvailableDocs(false);
    }
  };

  const closeAddDocsModal = () => setAddDocsModalOpen(false);

  const toggleSelectDoc = (docId) => {
    setSelectedDocIds((prev) =>
      prev.includes(docId) ? prev.filter((x) => x !== docId) : [...prev, docId]
    );
  };

  // Thêm là THAM CHIẾU (DEC-007) — tài liệu vẫn còn nguyên trong My Documents.
  const handleAddDocuments = async () => {
    if (selectedDocIds.length === 0) return;
    setAddingDocs(true);
    try {
      await collectionApi.addDocuments(id, selectedDocIds);
      showSuccess("Đã thêm tài liệu vào collection.");
      setAddDocsModalOpen(false);
      await load();
    } catch (err) {
      showError(err.message);
    } finally {
      setAddingDocs(false);
    }
  };

  return {
    collection,
    items: collection?.items ?? [],
    loading,
    error,

    removeDocument,
    removingDocId,

    addDocsModalOpen,
    availableDocs,
    loadingAvailableDocs,
    selectedDocIds,
    addingDocs,
    openAddDocsModal,
    closeAddDocsModal,
    toggleSelectDoc,
    handleAddDocuments,

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
  };
}
