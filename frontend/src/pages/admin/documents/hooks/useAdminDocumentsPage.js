import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as adminApi from "../../../../api/adminApi";
import * as documentApi from "../../../../api/documentApi";
import * as subjectApi from "../../../../api/subjectApi";
import { useToast } from "../../../../hooks/useToast";
import { useDebounce } from "../../../../hooks/useDebounce";

// Hook logic trang Admin QUẢN LÝ TÀI LIỆU: bảng có phân trang server, tìm kiếm/lọc (debounce từ
// khoá), duyệt/từ chối công khai, gỡ vi phạm (kèm modal xác nhận), sửa metadata, khôi phục, và
// xem chi tiết read-only.
export function useAdminDocumentsPage() {
  const { t } = useTranslation();
  const { showSuccess, showError } = useToast();
  const [searchParams, setSearchParams] = useSearchParams();
  const needsReview = searchParams.get("needsReview") === "true";

  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const [removeTarget, setRemoveTarget] = useState(null);
  const [removing, setRemoving] = useState(false);

  const [editTarget, setEditTarget] = useState(null);
  const [editing, setEditing] = useState(false);
  const [loadingEditTarget, setLoadingEditTarget] = useState(false);
  const [subjects, setSubjects] = useState([]);

  const [restoringId, setRestoringId] = useState(null);
  const [reviewing, setReviewing] = useState(null);

  const [detailTarget, setDetailTarget] = useState(null);
  const [loadingDetail, setLoadingDetail] = useState(false);

  // Tìm kiếm + lọc (chỉ áp dụng cho tab "Tất cả" — tab "Cần xem xét" luôn hiện đủ hàng chờ).
  // Từ khoá được debounce để không gọi API trên mỗi phím gõ.
  const [keyword, setKeyword] = useState("");
  const debouncedKeyword = useDebounce(keyword, 300);
  const [visibilityFilter, setVisibilityFilter] = useState("");
  const [moderationFilter, setModerationFilter] = useState("");
  // "" = tất cả (mặc định) | "ACTIVE" = đang hoạt động | "REMOVED" = đã gỡ.
  const [removedFilter, setRemovedFilter] = useState("");

  // Gọi API GET /admin/documents — nạp 1 trang kết quả theo toàn bộ bộ lọc hiện tại.
  const load = async (targetPage) => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminApi.listDocuments(
        targetPage,
        20,
        visibilityFilter || null,
        needsReview,
        debouncedKeyword.trim() || null,
        moderationFilter || null,
        removedFilter === "" ? null : removedFilter === "REMOVED"
      );
      setDocuments(data.content ?? []);
      setTotalPages(data.totalPages ?? 1);
    } catch (err) {
      setError(err.message);
      showError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load(page);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, needsReview, debouncedKeyword, visibilityFilter, moderationFilter, removedFilter]);

  // Mọi thay đổi bộ lọc đều quay về trang đầu — trang đang xem có thể không còn ở kết quả mới.
  const changeKeyword = (value) => {
    setKeyword(value);
    setPage(0);
  };

  const changeVisibilityFilter = (value) => {
    setVisibilityFilter(value);
    setPage(0);
  };

  const changeModerationFilter = (value) => {
    setModerationFilter(value);
    setPage(0);
  };

  const changeRemovedFilter = (value) => {
    setRemovedFilter(value);
    setPage(0);
  };

  const changeReviewFilter = (enabled) => {
    const nextParams = new URLSearchParams(searchParams);
    if (enabled) nextParams.set("needsReview", "true");
    else nextParams.delete("needsReview");
    setPage(0);
    setSearchParams(nextParams);
  };

  // Duyệt hoặc gỡ công khai một tài liệu đang chờ xem xét.
  const reviewDocument = async (doc, action) => {
    setReviewing({ id: doc.id, action });
    try {
      if (action === "approve") await adminApi.approveDocumentReview(doc.id);
      else await adminApi.removeDocumentReview(doc.id);
      showSuccess(action === "approve" ? t("admin.documents.docApproved") : t("admin.documents.docRemovedPublic"));
      await load(page);
    } catch (err) {
      showError(err.message);
    } finally {
      setReviewing(null);
    }
  };

  const openRemoveModal = (doc) => setRemoveTarget(doc);
  const closeRemoveModal = () => setRemoveTarget(null);

  // Gỡ tài liệu vi phạm (xác nhận trong modal trước).
  const confirmRemove = async () => {
    if (!removeTarget) return;
    setRemoving(true);
    try {
      await adminApi.removeDocument(removeTarget.id);
      showSuccess(t("admin.documents.docRemovedViolation"));
      setRemoveTarget(null);
      // Phân trang ở server -> tải lại đúng trang hiện tại thay vì tự suy state,
      // tránh lệch offset khi 1 dòng vừa biến mất khỏi trang cuối.
      await load(page);
    } catch (err) {
      showError(err.message);
    } finally {
      setRemoving(false);
    }
  };

  // AdminDocumentSummaryDTO (dòng bảng) không có subjectIds -> gọi GET /documents/{id}
  // (không kiểm tra ownership) để lấy đủ metadata cho form sửa.
  const openEditModal = async (doc) => {
    setEditTarget(doc);
    setLoadingEditTarget(true);
    try {
      const [full] = await Promise.all([
        documentApi.getOne(doc.id),
        subjects.length === 0 ? subjectApi.getAll().then(setSubjects) : Promise.resolve(),
      ]);
      setEditTarget(full);
    } catch (err) {
      showError(err.message);
      setEditTarget(null);
    } finally {
      setLoadingEditTarget(false);
    }
  };
  const closeEditModal = () => setEditTarget(null);

  // Lưu thay đổi metadata tài liệu từ modal sửa.
  const submitEdit = async (data) => {
    if (!editTarget) return;
    setEditing(true);
    try {
      await adminApi.updateDocument(editTarget.id, data);
      showSuccess(t("admin.documents.docUpdated"));
      setEditTarget(null);
      await load(page);
    } catch (err) {
      showError(err.message);
    } finally {
      setEditing(false);
    }
  };

  // Khôi phục là hành động an toàn/đảo ngược được (ngược lại của gỡ vi phạm) — không cần modal xác nhận.
  const restoreDocument = async (doc) => {
    setRestoringId(doc.id);
    try {
      await adminApi.restoreDocument(doc.id);
      showSuccess(t("admin.documents.docRestored"));
      await load(page);
    } catch (err) {
      showError(err.message);
    } finally {
      setRestoringId(null);
    }
  };

  // Chi tiết read-only — AdminDocumentSummaryDTO (dòng bảng) thiếu nhiều field (description,
  // subjectNames, moderationReason, fileName, favoriteCount...) nên gọi GET /documents/{id}
  // (đã có sẵn, không kiểm tra ownership) để lấy DocumentResponseDTO đầy đủ.
  const openDetailModal = async (doc) => {
    setDetailTarget(doc);
    setLoadingDetail(true);
    try {
      const full = await documentApi.getOne(doc.id);
      setDetailTarget(full);
    } catch (err) {
      showError(err.message);
      setDetailTarget(null);
    } finally {
      setLoadingDetail(false);
    }
  };
  const closeDetailModal = () => setDetailTarget(null);

  return {
    documents,
    loading,
    error,
    page,
    totalPages,
    setPage,
    needsReview,
    changeReviewFilter,
    keyword,
    changeKeyword,
    visibilityFilter,
    changeVisibilityFilter,
    moderationFilter,
    changeModerationFilter,
    removedFilter,
    changeRemovedFilter,
    removeTarget,
    removing,
    openRemoveModal,
    closeRemoveModal,
    confirmRemove,
    editTarget,
    editing,
    loadingEditTarget,
    subjects,
    openEditModal,
    closeEditModal,
    submitEdit,
    restoringId,
    restoreDocument,
    reviewing,
    reviewDocument,
    detailTarget,
    loadingDetail,
    openDetailModal,
    closeDetailModal,
  };
}
