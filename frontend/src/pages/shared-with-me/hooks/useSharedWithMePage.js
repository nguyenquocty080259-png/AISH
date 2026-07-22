import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as shareApi from "../../../api/shareApi";
import { useToast } from "../../../hooks/useToast";
import { ROUTES, buildRoute } from "../../../constants/routes";

export function useSharedWithMePage() {
  const navigate = useNavigate();
  const { showError } = useToast();

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try {
      const data = await shareApi.getSharedWithMe();
      setItems(Array.isArray(data) ? data : []);
    } catch (err) {
      showError(err.message);
      setItems([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const goToDocument = (documentId) => {
    navigate(buildRoute(ROUTES.DOCUMENT_DETAIL, { id: documentId }));
  };

  return { items, loading, goToDocument };
}
