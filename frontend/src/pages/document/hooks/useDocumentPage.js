import { useEffect, useMemo, useState } from "react";
import * as documentApi from "../../../api/documentApi";
import * as subjectApi from "../../../api/subjectApi";
import { useToast } from "../../../hooks/useToast";
import { useDebounce } from "../../../hooks/useDebounce";

const PAGE_SIZE = 8;

export function useDocumentPage() {
  const { showSuccess, showError } = useToast();

  const [documents, setDocuments] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(true);

  const [searchText, setSearchText] = useState("");
  const debouncedSearch = useDebounce(searchText, 350);
  const [subjectFilter, setSubjectFilter] = useState("");
  const [page, setPage] = useState(1);

  const [isUploadOpen, setIsUploadOpen] = useState(false);
  const [uploading, setUploading] = useState(false);

  const loadAll = async () => {
    setLoading(true);
    try {
      const [docs, subjectList] = await Promise.all([
        documentApi.getAll(),
        subjectApi.getAll(),
      ]);
      setDocuments(docs);
      setSubjects(subjectList);
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

  // Lọc phía client: theo tên (và mô tả) + môn học.
  const filteredDocuments = useMemo(() => {
    return documents.filter((doc) => {
      const keyword = debouncedSearch.trim().toLowerCase();
      const matchesKeyword =
        !keyword ||
        doc.title?.toLowerCase().includes(keyword) ||
        doc.description?.toLowerCase().includes(keyword);
      const matchesSubject =
        !subjectFilter || String(doc.subjectId) === String(subjectFilter);
      return matchesKeyword && matchesSubject;
    });
  }, [documents, debouncedSearch, subjectFilter]);

  const totalPages = Math.max(1, Math.ceil(filteredDocuments.length / PAGE_SIZE));
  const currentPage = Math.min(page, totalPages);
  const paginatedDocuments = filteredDocuments.slice(
    (currentPage - 1) * PAGE_SIZE,
    currentPage * PAGE_SIZE
  );

  const resetFilters = () => {
    setSearchText("");
    setSubjectFilter("");
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

  const handleUpload = async ({ title, description, subjectId, tags, file }) => {
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append("title", title);
      formData.append("description", description);
      if (subjectId) formData.append("subjectId", subjectId);
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
    searchText,
    setSearchText,
    subjectFilter,
    setSubjectFilter,
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