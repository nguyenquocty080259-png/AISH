import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as collectionApi from "../../../api/collectionApi";
import * as documentApi from "../../../api/documentApi";
import { useToast } from "../../../hooks/useToast";
import { ROUTES } from "../../../constants/routes";

// Bỏ dấu tiếng Việt để so khớp không phân biệt hoa/thường và có dấu/không dấu.
const COMBINING_MARKS_RE = /[̀-ͯ]/g;
function normalizeForSearch(text) {
  return (text ?? "")
    .normalize("NFD")
    .replace(COMBINING_MARKS_RE, "")
    .toLowerCase();
}

export function useCollectionDetailPage() {
  const { t } = useTranslation();
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

  const [docQuery, setDocQuery] = useState("");
  const [storageFilter, setStorageFilter] = useState("ALL");
  const [subjectFilter, setSubjectFilter] = useState("ALL");

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
      showSuccess(t("collectionDetail.renamed"));
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
      showSuccess(t("collectionDetail.deleted"));
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
      showSuccess(t("collectionDetail.removed"));
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
    setDocQuery("");
    setStorageFilter("ALL");
    setSubjectFilter("ALL");
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

  const closeAddDocsModal = () => {
    setAddDocsModalOpen(false);
    setDocQuery("");
    setStorageFilter("ALL");
    setSubjectFilter("ALL");
  };

  const toggleSelectDoc = (docId) => {
    setSelectedDocIds((prev) =>
      prev.includes(docId) ? prev.filter((x) => x !== docId) : [...prev, docId]
    );
  };

  // Danh sách môn học duy nhất xuất hiện trong availableDocs, để đổ vào dropdown lọc.
  const subjectOptions = useMemo(() => {
    const map = new Map();
    availableDocs.forEach((doc) => {
      (doc.subjectIds ?? []).forEach((subjectId, idx) => {
        const name = doc.subjectNames?.[idx];
        if (subjectId != null && name && !map.has(subjectId)) {
          map.set(subjectId, name);
        }
      });
    });
    return Array.from(map, ([id, name]) => ({ id, name })).sort((a, b) =>
      a.name.localeCompare(b.name, "vi")
    );
  }, [availableDocs]);

  const filteredAvailableDocs = useMemo(() => {
    const normalizedQuery = normalizeForSearch(docQuery);
    return availableDocs.filter((doc) => {
      if (normalizedQuery && !normalizeForSearch(doc.title).includes(normalizedQuery)) {
        return false;
      }
      if (storageFilter !== "ALL" && doc.storageType !== storageFilter && doc.storageType !== "BOTH") {
        return false;
      }
      if (subjectFilter !== "ALL" && !(doc.subjectIds ?? []).includes(subjectFilter)) {
        return false;
      }
      return true;
    });
  }, [availableDocs, docQuery, storageFilter, subjectFilter]);

  // Thêm là THAM CHIẾU (DEC-007) — tài liệu vẫn còn nguyên trong My Documents.
  const handleAddDocuments = async () => {
    if (selectedDocIds.length === 0) return;
    setAddingDocs(true);
    try {
      await collectionApi.addDocuments(id, selectedDocIds);
      showSuccess(t("collectionDetail.added"));
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
  };
}
