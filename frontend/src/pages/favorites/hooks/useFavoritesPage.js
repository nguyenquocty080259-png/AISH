import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as documentApi from "../../../api/documentApi";
import { useToast } from "../../../hooks/useToast";
import { ROUTES, buildRoute } from "../../../constants/routes";

export function useFavoritesPage() {
  const navigate = useNavigate();
  const { showError } = useToast();

  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await documentApi.listFavorites();
      setFavorites(data);
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

  // Bỏ thích: cùng endpoint toggle mà nút tim trên trang detail dùng. Bỏ tile khỏi
  // danh sách ngay (optimistic) thay vì refetch cả trang cho mượt.
  const unfavorite = async (documentId) => {
    try {
      await documentApi.toggleFavorite(documentId);
      setFavorites((prev) => prev.filter((d) => d.id !== documentId));
    } catch (err) {
      showError(err.message);
    }
  };

  const goToDocument = (documentId) => {
    navigate(buildRoute(ROUTES.DOCUMENT_DETAIL, { id: documentId }));
  };

  return {
    favorites,
    loading,
    error,
    unfavorite,
    goToDocument,
  };
}
