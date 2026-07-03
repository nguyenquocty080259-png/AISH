import { useEffect, useState } from "react";
import PageHeader from "../../../components/ui/PageHeader";
import Button from "../../../components/ui/Button";
import Modal from "../../../components/ui/Modal";
import EmptyState from "../../../components/ui/EmptyState";
import Table from "../../../components/ui/Table";
import { useAdminSubjectsPage } from "./hooks/useAdminSubjectsPage";
import "./admin-subjects.css";

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("vi-VN");
}

export default function AdminSubjectsPage() {
  const {
    subjects,
    loading,
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
  } = useAdminSubjectsPage();

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  const [renameName, setRenameName] = useState("");
  const [renameDescription, setRenameDescription] = useState("");

  useEffect(() => {
    if (renameTarget) {
      setRenameName(renameTarget.name ?? "");
      setRenameDescription(renameTarget.description ?? "");
    }
  }, [renameTarget]);

  const submitCreate = (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    createSubject({ name: name.trim(), description: description.trim() || undefined });
    setName("");
    setDescription("");
  };

  const submitRename = (e) => {
    e.preventDefault();
    if (!renameName.trim()) return;
    renameSubject({ name: renameName.trim(), description: renameDescription.trim() || undefined });
  };

  return (
    <div className="admin-subjects-page">
      <PageHeader
        title="Quản lý môn học"
        subtitle="Môn học là danh mục học thuật do Admin quản lý (DEC-030) — mỗi tài liệu cần thuộc ít nhất 1 môn."
        actions={
          <Button variant="primary" onClick={openCreateModal}>
            + Thêm môn học
          </Button>
        }
      />

      {loading ? (
        <p className="admin-subjects-page__loading">Đang tải danh sách môn học...</p>
      ) : subjects.length === 0 ? (
        <EmptyState
          icon="📚"
          message="Chưa có môn học nào."
          actionLabel="Thêm môn học"
          onAction={openCreateModal}
        />
      ) : (
        <Table>
          <Table.Head>
            <Table.Row>
              <Table.HeaderCell>Tên môn học</Table.HeaderCell>
              <Table.HeaderCell>Mô tả</Table.HeaderCell>
              <Table.HeaderCell>Ngày tạo</Table.HeaderCell>
              <Table.HeaderCell />
            </Table.Row>
          </Table.Head>
          <Table.Body>
            {subjects.map((subject) => (
              <Table.Row key={subject.id}>
                <Table.Cell>{subject.name}</Table.Cell>
                <Table.Cell className="ui-table__truncate">
                  {subject.description || "—"}
                </Table.Cell>
                <Table.Cell>{formatDate(subject.createdAt)}</Table.Cell>
                <Table.Cell>
                  <div className="ui-table__actions">
                    <Button variant="secondary" onClick={() => openRenameModal(subject)}>
                      Đổi tên
                    </Button>
                    <Button variant="danger" onClick={() => openDeleteModal(subject)}>
                      Xóa
                    </Button>
                  </div>
                </Table.Cell>
              </Table.Row>
            ))}
          </Table.Body>
        </Table>
      )}

      <Modal open={createModalOpen} onClose={closeCreateModal} title="Thêm môn học">
        <form className="admin-subjects-form" onSubmit={submitCreate}>
          <label className="admin-subjects-form__field">
            Tên môn học
            <input type="text" autoFocus value={name} onChange={(e) => setName(e.target.value)} />
          </label>
          <label className="admin-subjects-form__field">
            Mô tả (tuỳ chọn)
            <textarea rows={3} value={description} onChange={(e) => setDescription(e.target.value)} />
          </label>
          <div className="admin-subjects-form__actions">
            <Button type="button" variant="secondary" onClick={closeCreateModal} disabled={creating}>
              Hủy
            </Button>
            <Button type="submit" variant="primary" disabled={creating}>
              {creating ? "Đang thêm..." : "Thêm"}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal open={!!renameTarget} onClose={closeRenameModal} title="Đổi tên môn học">
        <form className="admin-subjects-form" onSubmit={submitRename}>
          <label className="admin-subjects-form__field">
            Tên môn học
            <input
              type="text"
              autoFocus
              value={renameName}
              onChange={(e) => setRenameName(e.target.value)}
            />
          </label>
          <label className="admin-subjects-form__field">
            Mô tả (tuỳ chọn)
            <textarea
              rows={3}
              value={renameDescription}
              onChange={(e) => setRenameDescription(e.target.value)}
            />
          </label>
          <div className="admin-subjects-form__actions">
            <Button type="button" variant="secondary" onClick={closeRenameModal} disabled={renaming}>
              Hủy
            </Button>
            <Button type="submit" variant="primary" disabled={renaming}>
              {renaming ? "Đang lưu..." : "Lưu"}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal open={!!deleteTarget} onClose={closeDeleteModal} title="Xóa môn học">
        <p className="admin-subjects-page__confirm-text">
          Xóa môn học "{deleteTarget?.name}"? Nếu môn học này đang được gán cho bất kỳ tài liệu
          nào, hệ thống sẽ từ chối xóa để tránh tài liệu bị mất hết môn học (DEC-030).
        </p>
        <div className="admin-subjects-form__actions">
          <Button variant="secondary" onClick={closeDeleteModal} disabled={deleting}>
            Hủy
          </Button>
          <Button variant="danger" onClick={deleteSubject} disabled={deleting}>
            {deleting ? "Đang xóa..." : "Xóa"}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
