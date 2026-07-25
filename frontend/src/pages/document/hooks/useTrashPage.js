import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import * as documentApi from "../../../api/documentApi";
import { useToast } from "../../../hooks/useToast";

export function useTrashPage() {
  const { t } = useTranslation();
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
      showSuccess(t("trash.restored"));
      await load();
    } catch (err) {
      showError(err.message);
    }
  };

  const handlePermanentDelete = async (id) => {
    if (!window.confirm(t("trash.confirmPermanent"))) return;
    try {
      await documentApi.permanentDelete(id);
      showSuccess(t("trash.permanentDeleted"));
      await load();
    } catch (err) {
      showError(err.message);
    }
  };

  return { documents, loading, handleRestore, handlePermanentDelete };
}