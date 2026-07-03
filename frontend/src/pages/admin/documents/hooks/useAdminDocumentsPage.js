import { useEffect, useState } from "react";
import * as adminApi from "../../../../api/adminApi";
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
  };
}
