import { useEffect, useState } from "react";
import * as documentApi from "../../../api/documentApi";
import { useToast } from "../../../hooks/useToast";

export function useTrashPage() {
  const { showSuccess, showError } = useToast();
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try {
      const docs = await documentApi.getTrash();
      setDocuments(docs);
    } catch (err) {
      showError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleRestore = async (id) => {
    try {
      await documentApi.restoreDocument(id);
      showSuccess("Đã khôi phục tài liệu.");
      await load();
    } catch (err) {
      showError(err.message);
    }
  };

  const handlePermanentDelete = async (id) => {
    if (!window.confirm("Xóa vĩnh viễn tài liệu này? Không thể khôi phục lại.")) return;
    try {
      await documentApi.permanentDelete(id);
      showSuccess("Đã xóa vĩnh viễn.");
      await load();
    } catch (err) {
      showError(err.message);
    }
  };

  return { documents, loading, handleRestore, handlePermanentDelete };
}