import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import * as adminApi from "../../../../api/adminApi";
import { useToast } from "../../../../hooks/useToast";
import { useSearchParams } from "react-router-dom";

// Hook logic trang Admin xử lý KHÁNG CÁO + BÌNH LUẬN CHỜ DUYỆT (2 tab, tab hiện tại lưu vào URL
// query để giữ khi tải lại trang).
export function useAdminAppealsPage() {
  const { t } = useTranslation();
  const { showSuccess, showError } = useToast();
  const [searchParams, setSearchParams] = useSearchParams();
  const [activeTab, setActiveTabState] = useState(
    searchParams.get("tab") === "comments" ? "comments" : "appeals"
  );

  const [appeals, setAppeals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState("");

  const [decisionTarget, setDecisionTarget] = useState(null); // { appeal, action: "approve"|"reject" }
  const [note, setNote] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [comments, setComments] = useState([]);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [commentPendingIds, setCommentPendingIds] = useState(() => new Set());

  // Gọi API GET /admin/appeals — nạp danh sách kháng cáo theo bộ lọc trạng thái.
  const load = async (status) => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminApi.listAppeals(status || undefined);
      setAppeals(data);
    } catch (err) {
      setError(err.message);
      showError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load(statusFilter);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [statusFilter]);

  // Gọi API GET /admin/comments?status=PENDING_REVIEW — nạp bình luận chờ duyệt.
  const loadComments = async () => {
    setCommentsLoading(true);
    try {
      setComments(await adminApi.listPendingComments());
    } catch (err) {
      showError(err.message);
    } finally {
      setCommentsLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === "comments") loadComments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab]);

  const setActiveTab = (tab) => {
    setActiveTabState(tab);
    const next = new URLSearchParams(searchParams);
    if (tab === "comments") next.set("tab", "comments");
    else next.delete("tab");
    setSearchParams(next, { replace: true });
  };

  const openDecisionModal = (appeal, action) => {
    setDecisionTarget({ appeal, action });
    setNote("");
  };

  const closeDecisionModal = () => setDecisionTarget(null);

  // Xác nhận duyệt/từ chối kháng cáo đang chọn trong modal.
  const submitDecision = async () => {
    if (!decisionTarget) return;
    const { appeal, action } = decisionTarget;
    setSubmitting(true);
    try {
      const updated =
        action === "approve"
          ? await adminApi.approveAppeal(appeal.appealId, note.trim())
          : await adminApi.rejectAppeal(appeal.appealId, note.trim());

      setAppeals((prev) => {
        // Nếu đang lọc theo 1 status cụ thể và kết quả không còn khớp filter đó nữa
        // (vd đang xem "Đang chờ" mà vừa duyệt xong) -> bỏ khỏi danh sách hiện tại.
        if (statusFilter && updated.status !== statusFilter) {
          return prev.filter((a) => a.appealId !== updated.appealId);
        }
        return prev.map((a) => (a.appealId === updated.appealId ? updated : a));
      });

      showSuccess(action === "approve" ? t("admin.appeals.appealApproved") : t("admin.appeals.appealRejected"));
      setDecisionTarget(null);
    } catch (err) {
      showError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  // Duyệt/từ chối 1 bình luận chờ duyệt, xoá khỏi danh sách sau khi xử lý xong.
  const reviewComment = async (comment, approve) => {
    setCommentPendingIds((previous) => new Set(previous).add(comment.id));
    try {
      if (approve) await adminApi.approveComment(comment.id);
      else await adminApi.rejectComment(comment.id);
      setComments((previous) => previous.filter((item) => item.id !== comment.id));
      showSuccess(approve ? t("admin.appeals.commentApproved") : t("admin.appeals.commentRejected"));
    } catch (err) {
      showError(err.message);
    } finally {
      setCommentPendingIds((previous) => {
        const next = new Set(previous);
        next.delete(comment.id);
        return next;
      });
    }
  };

  return {
    appeals,
    activeTab,
    setActiveTab,
    loading,
    error,
    statusFilter,
    setStatusFilter,
    decisionTarget,
    note,
    setNote,
    submitting,
    openDecisionModal,
    closeDecisionModal,
    submitDecision,
    comments,
    commentsLoading,
    commentPendingIds,
    reviewComment,
  };
}
