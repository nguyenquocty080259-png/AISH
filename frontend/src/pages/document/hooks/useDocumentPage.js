import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import * as documentApi from "../../../api/documentApi";
import * as subjectApi from "../../../api/subjectApi";
import { useToast } from "../../../hooks/useToast";
import { useDebounce } from "../../../hooks/useDebounce";

const PAGE_SIZE = 8;

/**
 * Hook chứa toàn bộ logic cho trang "Tài liệu của tôi": nạp danh sách tài liệu + môn học, lọc/
 * tìm kiếm/phân trang phía CLIENT (không gọi lại API mỗi lần lọc), và luồng tải tài liệu lên
 * (modal Upload). UI (DocumentPage.jsx) chỉ gọi các hàm/đọc state hook này trả về.
 */
export function useDocumentPage() {
  const { t } = useTranslation();
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

  // Gọi API lấy dung lượng đã dùng (cho thanh dung lượng trong modal upload).
  const loadStorageUsage = () => {
    documentApi
      .getStorageUsage()
      .then(setStorageUsage)
      .catch(() => setStorageUsage(null));
  };

  // Gọi API lấy danh sách đuôi tệp được phép tải lên.
  const loadAllowedFileTypes = () => {
    documentApi
      .getAllowedFileTypes()
      .then((data) => setAllowedFileTypes(data.allowedExtensions || null))
      .catch(() => setAllowedFileTypes(null));
  };

  // Nạp lại toàn bộ dữ liệu trang: tài liệu của tôi + danh sách môn học (gọi song song).
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

  // Đổi từ khoá / môn học là đổi hẳn tập kết quả, nên số trang cũ không còn ý nghĩa: đang ở
  // trang 3 mà lọc lại thì người dùng rơi vào giữa danh sách mới (hoặc trang cuối bị kẹp lại
  // bởi Math.min bên dưới) và tưởng là không có kết quả. Luôn về trang 1 khi bộ lọc đổi.
  useEffect(() => {
    setPage(1);
  }, [debouncedSearch, subjectFilter]);

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

  // Gọi API bật/tắt yêu thích rồi nạp lại danh sách để cập nhật giao diện.
  const handleToggleFavorite = async (id) => {
    try {
      await documentApi.toggleFavorite(id);
      await loadAll();
    } catch (err) {
      showError(err.message);
    }
  };

  // Xử lý form Upload: dựng FormData rồi gọi API tải tài liệu lên. Thành công thì báo tin nhắn
  // theo đúng nơi lưu (LOCAL/CLOUD/BOTH), đóng modal, và nạp lại danh sách.
  const handleUpload = async ({ title, description, subjectIds, file, storage }) => {
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append("title", title);
      formData.append("description", description);
      (subjectIds || []).forEach((id) => formData.append("subjectIds", id));
      formData.append("storage", storage || "SERVER");
      formData.append("file", file);

      await documentApi.upload(formData);
      const msg =
        storage === "BOTH" ? t("documents.upload_modal.uploadSuccessBoth") :
        storage === "CLOUD" ? t("documents.upload_modal.uploadSuccessCloud") :
        t("documents.upload_modal.uploadSuccess");
      showSuccess(msg);
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