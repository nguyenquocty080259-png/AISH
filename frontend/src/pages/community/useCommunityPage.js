import { useEffect, useState } from "react";
import * as documentApi from "../../api/documentApi";
import * as subjectApi from "../../api/subjectApi";
import { useToast } from "../../hooks/useToast";
import { useDebounce } from "../../hooks/useDebounce";

// Hook logic trang Cộng đồng: nạp danh sách môn học 1 lần, gọi API tìm kiếm mỗi khi bộ lọc/trang
// đổi (debounce riêng cho từ khoá), và tự về trang 0 khi đổi bộ lọc (trừ đổi trang).
export function useCommunityPage() {
  const { showError } = useToast();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState("");
  const [sortBy, setSortBy] = useState("newest");
  const [subjectId, setSubjectId] = useState("");
  const [minRating, setMinRating] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const [subjects, setSubjects] = useState([]);

  const debounced = useDebounce(keyword, 350);

  useEffect(() => {
    subjectApi.getAll().then(setSubjects).catch(() => setSubjects([]));
  }, []);

  // Gọi API GET /documents/community — nạp trang kết quả theo bộ lọc/sắp xếp hiện tại.
  const load = async () => {
    setLoading(true);
    try {
      const data = await documentApi.getCommunity({
        keyword: debounced || undefined,
        subjectId: subjectId || undefined,
        minRating: minRating || undefined,
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
  }, [debounced, sortBy, subjectId, minRating, page]);

  useEffect(() => {
    setPage(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debounced, sortBy, subjectId, minRating]);

  const resetFilters = () => {
    setKeyword("");
    setSortBy("newest");
    setSubjectId("");
    setMinRating("");
  };

  return {
    items,
    loading,
    keyword,
    setKeyword,
    sortBy,
    setSortBy,
    subjectId,
    setSubjectId,
    minRating,
    setMinRating,
    subjects,
    resetFilters,
    page,
    setPage,
    totalPages,
  };
}
