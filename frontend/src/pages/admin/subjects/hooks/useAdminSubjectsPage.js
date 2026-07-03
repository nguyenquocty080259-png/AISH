import { useEffect, useState } from "react";
import * as subjectApi from "../../../../api/subjectApi";
import { useToast } from "../../../../hooks/useToast";

export function useAdminSubjectsPage() {
  const { showSuccess, showError } = useToast();

  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [creating, setCreating] = useState(false);

  const [renameTarget, setRenameTarget] = useState(null);
  const [renaming, setRenaming] = useState(false);

  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await subjectApi.getAll();
      setSubjects(data);
    } catch (err) {
      setError(err.message);
      showError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const openCreateModal = () => setCreateModalOpen(true);
  const closeCreateModal = () => setCreateModalOpen(false);

  const createSubject = async ({ name, description }) => {
    setCreating(true);
    try {
      const created = await subjectApi.create({ name, description });
      // Backend là findOrCreate -> trùng tên trả về subject CŨ đã có, không tạo bản mới.
      setSubjects((prev) => (prev.some((s) => s.id === created.id) ? prev : [...prev, created]));
      showSuccess("Đã thêm môn học.");
      setCreateModalOpen(false);
    } catch (err) {
      showError(err.message);
    } finally {
      setCreating(false);
    }
  };

  const openRenameModal = (subject) => setRenameTarget(subject);
  const closeRenameModal = () => setRenameTarget(null);

  const renameSubject = async ({ name, description }) => {
    if (!renameTarget) return;
    setRenaming(true);
    try {
      const updated = await subjectApi.update(renameTarget.id, { name, description });
      setSubjects((prev) => prev.map((s) => (s.id === updated.id ? updated : s)));
      showSuccess("Đã cập nhật môn học.");
      setRenameTarget(null);
    } catch (err) {
      // 400 (tên rỗng) / 409 (trùng tên) đến từ GlobalExceptionHandler -> hiện đúng message thật.
      showError(err.message);
    } finally {
      setRenaming(false);
    }
  };

  const openDeleteModal = (subject) => setDeleteTarget(subject);
  const closeDeleteModal = () => setDeleteTarget(null);

  const deleteSubject = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await subjectApi.remove(deleteTarget.id);
      setSubjects((prev) => prev.filter((s) => s.id !== deleteTarget.id));
      showSuccess("Đã xóa môn học.");
      setDeleteTarget(null);
    } catch (err) {
      // DEC-030: 409 "đang được gán cho tài liệu" phải hiện nguyên văn, không nuốt lỗi.
      showError(err.message);
    } finally {
      setDeleting(false);
    }
  };

  return {
    subjects,
    loading,
    error,

    createModalOpen,
    creating,
    openCreateModal,
    closeCreateModal,
    createSubject,

    renameTarget,
    renaming,
    openRenameModal,
    closeRenameModal,
    renameSubject,

    deleteTarget,
    deleting,
    openDeleteModal,
    closeDeleteModal,
    deleteSubject,
  };
}
