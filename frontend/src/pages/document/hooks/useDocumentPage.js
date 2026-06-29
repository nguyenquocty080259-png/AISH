import { useEffect, useMemo, useState } from "react";
import * as documentApi from "../../../api/documentApi";
import * as subjectApi from "../../../api/subjectApi";
import * as tagApi from "../../../api/tagApi";
import { useToast } from "../../../hooks/useToast";
import { useDebounce } from "../../../hooks/useDebounce";

const PAGE_SIZE = 8;

export function useDocumentPage() {
  const { showSuccess, showError } = useToast();

  const [documents, setDocuments] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [tags, setTags] = useState([]);
  const [loading, setLoading] = useState(true);

  const [searchText, setSearchText] = useState("");
  const debouncedSearch = useDebounce(searchText, 350);
  const [subjectFilter, setSubjectFilter] = useState("");
  const [tagFilter, setTagFilter] = useState("");
  const [page, setPage] = useState(1);

  const [isUploadOpen, setIsUploadOpen] = useState(false);
  const [uploading, setUploading] = useState(false);

  const loadAll = async () => {
    setLoading(true);
    try {
      const [docs, subjectList, tagList] = await Promise.all([
        documentApi.getAll(),
        subjectApi.getAll(),
        tagApi.getAll(),
      ]);
      setDocuments(docs);
      setSubjects(subjectList);
      setTags(tagList);
    } catch (err) {
      showError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Backend hiện chưa hỗ trợ search/filter qua query param nên xử lý phía client.
  const filteredDocuments = useMemo(() => {
    return documents.filter((doc) => {
      const keyword = debouncedSearch.trim().toLowerCase();
      const matchesKeyword =
        !keyword ||
        doc.title?.toLowerCase().includes(keyword) ||
        doc.description?.toLowerCase().includes(keyword);
      const matchesSubject =
        !subjectFilter || String(doc.subjectId) === String(subjectFilter);
      const matchesTag = !tagFilter || doc.tags?.includes(tagFilter);
      return matchesKeyword && matchesSubject && matchesTag;
    });
  }, [documents, debouncedSearch, subjectFilter, tagFilter]);

  const totalPages = Math.max(1, Math.ceil(filteredDocuments.length / PAGE_SIZE));
  const currentPage = Math.min(page, totalPages);
  const paginatedDocuments = filteredDocuments.slice(
    (currentPage - 1) * PAGE_SIZE,
    currentPage * PAGE_SIZE
  );

  const resetFilters = () => {
    setSearchText("");
    setSubjectFilter("");
    setTagFilter("");
    setPage(1);
  };

  const handleToggleFavorite = async (id) => {
    try {
      await documentApi.toggleFavorite(id);
      await loadAll();
    } catch (err) {
      showError(err.message);
    }
  };

  const handleUpload = async ({
    title,
    description,
    subjectId,
    newSubjectName,
    tags,
    file,
  }) => {
    setUploading(true);
    try {
      let resolvedSubjectId = subjectId || null;

      // Nếu user nhập môn học mới (chưa có trong danh sách), tạo trước để lấy id.
      if (!resolvedSubjectId && newSubjectName?.trim()) {
        const subject = await subjectApi.create({ name: newSubjectName.trim() });
        resolvedSubjectId = subject.id;
      }

      const formData = new FormData();
      formData.append("title", title);
      formData.append("description", description);
      if (resolvedSubjectId) formData.append("subjectId", resolvedSubjectId);
      tags.forEach((tag) => formData.append("tags", tag));
      formData.append("file", file);

      await documentApi.upload(formData);
      showSuccess("Tải lên tài liệu thành công.");
      setIsUploadOpen(false);
      await loadAll();
    } catch (err) {
      showError(err.message);
    } finally {
      setUploading(false);
    }
  };

  return {
    loading,
    documents: paginatedDocuments,
    subjects,
    tags,
    searchText,
    setSearchText,
    subjectFilter,
    setSubjectFilter,
    tagFilter,
    setTagFilter,
    resetFilters,
    page: currentPage,
    totalPages,
    setPage,
    isUploadOpen,
    setIsUploadOpen,
    uploading,
    handleUpload,
    handleToggleFavorite,
  };
}
