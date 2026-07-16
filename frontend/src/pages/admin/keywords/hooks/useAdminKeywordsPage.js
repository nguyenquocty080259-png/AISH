import { useEffect, useState } from "react";
import * as adminApi from "../../../../api/adminApi";
import { useToast } from "../../../../hooks/useToast";

export const KEYWORD_TYPES = [
  { value: "AI_CHAT", label: "AI Chat" },
  { value: "COMMENT", label: "Bình luận" },
  { value: "NAMING", label: "Đặt tên" },
  { value: "DOCUMENT_CONTENT", label: "Nội dung tài liệu" },
];

export function useAdminKeywordsPage() {
  const { showSuccess, showError } = useToast();
  const [activeType, setActiveType] = useState(KEYWORD_TYPES[0].value);
  const [keywords, setKeywords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [pendingIds, setPendingIds] = useState(() => new Set());
  const [editTarget, setEditTarget] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const load = async () => {
    setLoading(true);
    try {
      setKeywords(await adminApi.listModerationKeywords(activeType));
    } catch (error) {
      setKeywords([]);
      showError(error.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // The selected type is the only value that should trigger a reload.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeType]);

  const markPending = (id, pending) => {
    setPendingIds((previous) => {
      const next = new Set(previous);
      if (pending) next.add(id);
      else next.delete(id);
      return next;
    });
  };

  const createKeyword = async (keyword) => {
    setCreating(true);
    try {
      const created = await adminApi.createModerationKeyword({ keyword, type: activeType });
      setKeywords((previous) => [created, ...previous]);
      showSuccess("Đã thêm từ khóa.");
      return true;
    } catch (error) {
      showError(error.message);
      return false;
    } finally {
      setCreating(false);
    }
  };

  const updateKeyword = async (id, payload, successMessage) => {
    markPending(id, true);
    try {
      const updated = await adminApi.updateModerationKeyword(id, payload);
      setKeywords((previous) => previous.map((item) => (item.id === id ? updated : item)));
      showSuccess(successMessage);
      return true;
    } catch (error) {
      showError(error.message);
      return false;
    } finally {
      markPending(id, false);
    }
  };

  const toggleActive = (item) =>
    updateKeyword(
      item.id,
      { active: !item.active },
      item.active ? "Đã tắt từ khóa." : "Đã bật từ khóa."
    );

  const saveEdit = async (keyword) => {
    if (!editTarget) return false;
    const saved = await updateKeyword(editTarget.id, { keyword }, "Đã cập nhật từ khóa.");
    if (saved) setEditTarget(null);
    return saved;
  };

  const deleteKeyword = async () => {
    if (!deleteTarget) return;
    const id = deleteTarget.id;
    markPending(id, true);
    try {
      await adminApi.deleteModerationKeyword(id);
      setKeywords((previous) => previous.filter((item) => item.id !== id));
      setDeleteTarget(null);
      showSuccess("Đã xóa từ khóa.");
    } catch (error) {
      showError(error.message);
    } finally {
      markPending(id, false);
    }
  };

  return {
    activeType,
    setActiveType,
    keywords,
    loading,
    creating,
    pendingIds,
    createKeyword,
    toggleActive,
    editTarget,
    setEditTarget,
    saveEdit,
    deleteTarget,
    setDeleteTarget,
    deleteKeyword,
  };
}
