import { useEffect, useState } from "react";
import * as documentApi from "../../api/documentApi";
import * as subjectApi from "../../api/subjectApi";
import * as tagApi from "../../api/tagApi";
import { useToast } from "../../hooks/useToast";
import { useDebounce } from "../../hooks/useDebounce";

export function useCommunityPage() {
  const { showError } = useToast();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState("");
  const [sortBy, setSortBy] = useState("newest");
  const [subjectId, setSubjectId] = useState("");
  const [tagId, setTagId] = useState("");
  const [minRating, setMinRating] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const [subjects, setSubjects] = useState([]);
  const [tags, setTags] = useState([]);

  const debounced = useDebounce(keyword, 350);

  useEffect(() => {
    subjectApi.getAll().then(setSubjects).catch(() => setSubjects([]));
    tagApi.getAll().then(setTags).catch(() => setTags([]));
  }, []);

  const load = async () => {
    setLoading(true);
    try {
      const data = await documentApi.getCommunity({
        keyword: debounced || undefined,
        subjectId: subjectId || undefined,
        tagId: tagId || undefined,
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
  }, [debounced, sortBy, subjectId, tagId, minRating, page]);

  useEffect(() => {
    setPage(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debounced, sortBy, subjectId, tagId, minRating]);

  const resetFilters = () => {
    setKeyword("");
    setSortBy("newest");
    setSubjectId("");
    setTagId("");
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
    tagId,
    setTagId,
    minRating,
    setMinRating,
    subjects,
    tags,
    resetFilters,
    page,
    setPage,
    totalPages,
  };
}