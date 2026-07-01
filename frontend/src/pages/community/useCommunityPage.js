import { useEffect, useState } from "react";
import * as documentApi from "../../api/documentApi";
import { useToast } from "../../hooks/useToast";
import { useDebounce } from "../../hooks/useDebounce";

export function useCommunityPage() {
  const { showError } = useToast();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState("");
  const [sortBy, setSortBy] = useState("newest");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const debounced = useDebounce(keyword, 350);

  const load = async () => {
    setLoading(true);
    try {
      const data = await documentApi.getCommunity({
        keyword: debounced || undefined,
        sortBy,
        page,
        size: 12,
      });
      setItems(Array.isArray(data?.items) ? data.items : []);
      setTotalPages(data?.totalPages ?? 1);
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
  }, [debounced, sortBy, page]);

  return { items, loading, keyword, setKeyword, sortBy, setSortBy, page, setPage, totalPages };
}