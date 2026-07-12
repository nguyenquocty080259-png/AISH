import { useEffect, useState } from "react";
import * as adminApi from "../../../../api/adminApi";
import * as documentApi from "../../../../api/documentApi";
import * as subjectApi from "../../../../api/subjectApi";
import { useToast } from "../../../../hooks/useToast";

export function useAdminDocumentsPage() {
  const { showSuccess, showError } = useToast();

  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const [removeTarget, setRemoveTarget] = useState(null);
  const [removing, setRemoving] = useState(false);

  const [editTarget, setEditTarget] = useState(null);
  const [editing, setEditing] = useState(false);
  const [loadingEditTarget, setLoadingEditTarget] = useState(false);
  const [subjects, setSubjects] = useState([]);

  const [restoringId, setRestoringId] = useState(null);

  const load = async (targetPage) => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminApi.listDocuments(targetPage);
      setDocuments(data.content ?? []);
      setTotalPages(data.totalPages ?? 1);
    } catch (err) {
      setError(err.message);
      showError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load(page);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  const openRemoveModal = (doc) => setRemoveTarget(doc);
  const closeRemoveModal = () => setRemoveTarget(null);

  const confirmRemove = async () => {
    if (!removeTarget) return;
    setRemoving(true);
    try {
      await adminApi.removeDocument(removeTarget.id);
      showSuccess("Đã gỡ tài liệu vi phạm.");
      setRemoveTarget(null);
      // Phân trang ở server -> tải lại đúng trang hiện tại thay vì tự suy state,
      // tránh lệch offset khi 1 dòng vừa biến mất khỏi trang cuối.
      await load(page);
    } catch (err) {
      showError(err.message);
    } finally {
      setRemoving(false);
    }
  };

  // AdminDocumentSummaryDTO (dòng bảng) không có subjectIds -> gọi GET /documents/{id}
  // (không kiểm tra ownership) để lấy đủ metadata cho form sửa.
  const openEditModal = async (doc) => {
    setEditTarget(doc);
    setLoadingEditTarget(true);
    try {
      const [full] = await Promise.all([
        documentApi.getOne(doc.id),
        subjects.length === 0 ? subjectApi.getAll().then(setSubjects) : Promise.resolve(),
      ]);
      setEditTarget(full);
    } catch (err) {
      showError(err.message);
      setEditTarget(null);
    } finally {
      setLoadingEditTarget(false);
    }
  };
  const closeEditModal = () => setEditTarget(null);

  const submitEdit = async (data) => {
    if (!editTarget) return;
    setEditing(true);
    try {
      await adminApi.updateDocument(editTarget.id, data);
      showSuccess("Đã cập nhật tài liệu.");
      setEditTarget(null);
      await load(page);
    } catch (err) {
      showError(err.message);
    } finally {
      setEditing(false);
    }
  };

  // Khôi phục là hành động an toàn/đảo ngược được (ngược lại của gỡ vi phạm) — không cần modal xác nhận.
  const restoreDocument = async (doc) => {
    setRestoringId(doc.id);
    try {
      await adminApi.restoreDocument(doc.id);
      showSuccess("Đã khôi phục tài liệu.");
      await load(page);
    } catch (err) {
      showError(err.message);
    } finally {
      setRestoringId(null);
    }
  };

  return {
    documents,
    loading,
    error,
    page,
    totalPages,
    setPage,
    removeTarget,
    removing,
    openRemoveModal,
    closeRemoveModal,
    confirmRemove,
    editTarget,
    editing,
    loadingEditTarget,
    subjects,
    openEditModal,
    closeEditModal,
    submitEdit,
    restoringId,
    restoreDocument,
  };
}
