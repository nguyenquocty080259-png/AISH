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
  // null = chưa biết / lấy thất bại -> UploadModal không chặn gì, để BE tự quyết định
  // (fail-open, giống tinh thần fail-safe của SystemSettingService ở BE).
  const [storageUsage, setStorageUsage] = useState(null);
  // null = chưa biết / lấy thất bại -> UploadModal không set accept và không tiền-kiểm đuôi,
  // để BE tự quyết (fail-open, giống storageUsage).
  const [allowedFileTypes, setAllowedFileTypes] = useState(null);

  const loadStorageUsage = () => {
    documentApi
      .getStorageUsage()
      .then(setStorageUsage)
      .catch(() => setStorageUsage(null));
  };

  const loadAllowedFileTypes = () => {
    documentApi
      .getAllowedFileTypes()
      .then((data) => setAllowedFileTypes(data.allowedExtensions || null))
      .catch(() => setAllowedFileTypes(null));
  };

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
    loadStorageUsage();
  };

  useEffect(() => {
    loadAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Lấy lại dung lượng đã dùng mỗi lần mở modal upload (ngoài lần tải khi vào trang) để form
  // luôn có số mới nhất nếu admin vừa đổi cài đặt hoặc user vừa xóa tài liệu vĩnh viễn ở tab khác.
  useEffect(() => {
    if (!isUploadOpen) return;
    loadStorageUsage();
    // Lấy allowlist mới nhất mỗi lần mở modal, để admin vừa đổi là user thấy ngay.
    loadAllowedFileTypes();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isUploadOpen]);

  // Lọc client: theo tên + mô tả + môn học (môn là mảng subjectIds)
  const filteredDocuments = useMemo(() => {
    return documents.filter((doc) => {
      const keyword = debouncedSearch.trim().toLowerCase();
      const matchesKeyword =
        !keyword ||
        doc.title?.toLowerCase().includes(keyword) ||
        doc.description?.toLowerCase().includes(keyword) ||
        (doc.subjectNames || []).some((name) =>
          name.toLowerCase().includes(keyword)
        );
      const matchesSubject =
        !subjectFilter ||
        (doc.subjectIds || []).map(String).includes(String(subjectFilter));
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

  const handleUpload = async ({ title, description, subjectIds, file, storage }) => {
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append("title", title);
      formData.append("description", description);
      (subjectIds || []).forEach((id) => formData.append("subjectIds", id));
      formData.append("storage", storage || "LOCAL");
      formData.append("file", file);

      await documentApi.upload(formData);
      const label =
        storage === "BOTH" ? " (lưu cả server và cloud)" :
        storage === "CLOUD" ? " (lưu trên cloud)" : "";
      showSuccess("Tải lên tài liệu thành công." + label);
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
    storageUsage,
    allowedFileTypes,
    handleUpload,
    handleToggleFavorite,
  };
}