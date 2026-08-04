import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as shareApi from "../../../api/shareApi";
import { useToast } from "../../../hooks/useToast";
import { ROUTES, buildRoute } from "../../../constants/routes";

// Hook logic trang "Chia sẻ với tôi": nạp danh sách tài liệu người khác chia sẻ, điều hướng sang
// trang chi tiết khi bấm vào.
export function useSharedWithMePage() {
  const navigate = useNavigate();
  const { showError } = useToast();

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  // Gọi API GET /documents/shared-with-me — nạp danh sách tài liệu được chia sẻ với tôi.
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
